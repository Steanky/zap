package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PathfinderEngine implements Listener {
    public static class Entry {
        public final PathOperation operation;
        public final CompletableFuture<PathResult> future;

        private Entry(@NotNull PathOperation operation, @NotNull CompletableFuture<PathResult> future) {
            this.operation = Objects.requireNonNull(operation, "operation cannot be null!");
            this.future = Objects.requireNonNull(future,"future cannot be null!");
        }
    }

    private static class EngineContext implements PathfinderContext {
        private final Semaphore contextSemaphore = new Semaphore(-1);

        private final List<Entry> operations = new ArrayList<>();
        private final List<PathResult> successfulPaths = new ArrayList<>();
        private final List<PathResult> failedPaths = new ArrayList<>();
        private final SnapshotProvider provider;

        public EngineContext(@NotNull SnapshotProvider provider) {
            this.provider = Objects.requireNonNull(provider, "provider cannot be null!");
        }

        @Override
        public @NotNull List<Entry> ongoingOperations() {
            return operations;
        }

        @Override
        public @NotNull List<PathResult> successfulPaths() {
            return successfulPaths;
        }

        @Override
        public @NotNull List<PathResult> failedPaths() {
            return failedPaths;
        }

        @Override
        public @NotNull SnapshotProvider snapshotProvider() {
            return provider;
        }
    }

    private final List<EngineContext> contexts = new ArrayList<>();
    private final Semaphore contextsSemaphore = new Semaphore(-1);

    public PathfinderEngine() {
        Thread pathfinderThread = new Thread(null, this::pathfind, "Pathfinder");
        pathfinderThread.start();

        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    /**
     * Method responsible for all the pathfinding, for each world. Eventually it may be necessary to split each world
     * into a separate thread or use an ExecutorService.
     */
    private void pathfind() {
        while(true) {
            try {
                contextsSemaphore.acquireUninterruptibly();

                int contextStartingIndex;
                synchronized (contexts) {
                    contextStartingIndex = contexts.size() - 1;
                }

                for(int i = contextStartingIndex; i > -1; i--) { //each EngineContext object = different world
                    EngineContext context = contexts.get(i);

                    AtomicBoolean completed = new AtomicBoolean(false);
                    context.contextSemaphore.tryAcquire(1);

                    Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> { //syncing must be run on main thread
                        context.provider.syncWithWorld();
                        completed.set(true);
                        context.contextSemaphore.release();
                    });

                    context.contextSemaphore.acquireUninterruptibly(); //wait for main thread to finish syncing
                    context.contextSemaphore.release(); //reset the semaphore

                    int operationStartingIndex;

                    synchronized (context) {
                        operationStartingIndex = context.operations.size() - 1;
                    }

                    for(int j = operationStartingIndex; j > -1; j--) { //iterate all pathfinding operations for this world
                        Entry entry = context.operations.get(j);

                        PathState entryState = entry.operation.getState();
                        if(entryState == PathState.INCOMPLETE) {
                            for(int k = 0; k < entry.operation.desiredIterations(); k++) {
                                if(entry.operation.step(context)) {
                                    PathResult result = entry.operation.getResult();

                                    if(entry.operation.getState() == PathState.SUCCEEDED) {
                                        context.successfulPaths.add(result);
                                    }
                                    else if(entry.operation.getState() == PathState.FAILED) {
                                        context.failedPaths.add(result);
                                    }
                                    else {
                                        ArenaApi.severe("PathResult " + result.toString() + " has an invalid state: " +
                                                "should be either SUCCEEDED or FAILED, but was " +
                                                entry.operation.getState().toString());
                                        continue;
                                    }

                                    if(!entry.future.complete(result)) {
                                        ArenaApi.severe("Failed to properly complete the future for PathResult " +
                                                result.toString());
                                    }

                                    break;
                                }
                            }
                        }
                        else if(entry.operation.shouldRemove()) { //remove successful or failed paths if they ask
                            PathResult entryResult = entry.operation.getResult();

                            if(entryState == PathState.SUCCEEDED) {
                                context.successfulPaths.remove(entryResult);
                            }
                            else if(entryState == PathState.FAILED) {
                                context.failedPaths.remove(entryResult);
                            }
                        }
                    }
                }

                synchronized (contexts) {
                    if(contexts.size() > 0) { //if we have more to iterate, release semaphore...
                        contextsSemaphore.release();
                    }
                    else {
                        //noinspection ResultOfMethodCallIgnored
                        contextsSemaphore.tryAcquire(1); //...if we don't, lock semaphore so we wait
                    }
                }
            }
            catch (Exception exception) {
                ArenaApi.warning("Exception occurred in pathfinding thread: " + exception.getMessage());
                ArenaApi.warning("Clearing state and continuing.");

                synchronized (contexts) {
                    contexts.clear();
                    //noinspection ResultOfMethodCallIgnored
                    contextsSemaphore.tryAcquire(1);
                }
            }
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is not thread-safe and should only be
     * called on the main server thread.
     * @param operation The operation to enqueue
     */
    public Future<PathResult> queueOperation(@NotNull PathOperation operation) {
        Objects.requireNonNull(operation, "operation cannot be null!");

        CompletableFuture<PathResult> future = new CompletableFuture<>();

        EngineContext targetContext = null;
        synchronized (contexts) {
            for(EngineContext context : contexts) {
                if(context.snapshotProvider().getWorld().equals(operation.getWorld())) {
                    targetContext = context;
                    break;
                }
            }
        }

        if(targetContext == null) {
            targetContext = new EngineContext(new WorldSnapshotProvider(operation.getWorld()));
            targetContext.operations.add(new Entry(operation, future));

            synchronized (contexts) {
                contexts.add(targetContext);
                contextsSemaphore.release();
            }
        }
        else {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (targetContext) {
                targetContext.operations.add(new Entry(operation, future));
            }
        }

        return future;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        synchronized (contexts) {
            for(int i = contexts.size() - 1; i > -1; i--) {
                EngineContext context = contexts.get(i);

                if(context.provider.getWorld().equals(event.getWorld())) {
                    contexts.remove(i);
                }
            }
        }
    }
}