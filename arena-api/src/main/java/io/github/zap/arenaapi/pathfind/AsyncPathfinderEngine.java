package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.ObjectDisposedException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncPathfinderEngine implements PathfinderEngine, Listener {
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
        private final SnapshotProvider snapshot;

        public EngineContext(@NotNull SnapshotProvider snapshot) {
            this.snapshot = Objects.requireNonNull(snapshot, "provider cannot be null!");
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
            return snapshot;
        }
    }

    private final Thread pathfinderThread;
    private final List<EngineContext> contexts = new ArrayList<>();
    private final Semaphore contextsSemaphore = new Semaphore(-1);
    private final Queue<EngineContext> removalQueue = new ArrayDeque<>();

    private boolean disposed = false;
    private boolean shouldRun = true;

    public AsyncPathfinderEngine() {
        pathfinderThread = new Thread(null, this::pathfind, "Pathfinder");
        pathfinderThread.start();

        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    /**
     * Method responsible for all the pathfinding, for each world. Eventually it may be necessary to split each world
     * into a separate thread or use an ExecutorService.
     */
    private void pathfind() {
        while(shouldRun) {
            try {
                try {
                    contextsSemaphore.acquire();

                    int contextStartingIndex;
                    synchronized (contexts) {
                        contextStartingIndex = contexts.size() - 1;
                    }

                    for(int i = contextStartingIndex; i > -1; i--) { //each EngineContext object = different world
                        EngineContext context = contexts.get(i);

                        AtomicBoolean completed = new AtomicBoolean(false);

                        Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> { //syncing must be run on main thread
                            context.snapshot.syncWithWorld();
                            completed.set(true);
                            context.contextSemaphore.release();
                        });

                        context.contextSemaphore.acquire(); //wait for main thread to finish syncing
                        context.contextSemaphore.release(); //reset the semaphore

                        int operationStartingIndex;
                        synchronized (context) {
                            operationStartingIndex = context.operations.size() - 1;
                        }

                        for(int j = operationStartingIndex; j > -1; j--) { //iterate all pathfinding operations for this world
                            Entry entry = context.operations.get(j);

                            PathOperation.State entryState = entry.operation.getState();
                            if(entryState == PathOperation.State.INCOMPLETE) {
                                for(int k = 0; k < entry.operation.desiredIterations(); k++) {
                                    if(entry.operation.step(context)) {
                                        PathResult result = entry.operation.getResult();

                                        if(entry.operation.getState() == PathOperation.State.SUCCEEDED) {
                                            context.successfulPaths.add(result);
                                        }
                                        else if(entry.operation.getState() == PathOperation.State.FAILED) {
                                            context.failedPaths.add(result);
                                        }
                                        else {
                                            ArenaApi.severe("PathOperation " + entry.operation + " has an invalid state: " +
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

                                    if(Thread.interrupted()) {
                                        throw new InterruptedException();
                                    }
                                }
                            }
                            else if(entry.operation.shouldRemove()) { //remove successful or failed paths if they ask
                                PathResult entryResult = entry.operation.getResult();

                                if(entryState == PathOperation.State.SUCCEEDED) {
                                    context.successfulPaths.remove(entryResult);
                                }
                                else if(entryState == PathOperation.State.FAILED) {
                                    context.failedPaths.remove(entryResult);
                                }
                            }
                        }

                        synchronized (removalQueue) {
                            while(!removalQueue.isEmpty()) {
                                contexts.remove(removalQueue.remove());
                            }
                        }
                    }

                    synchronized (contexts) {
                        if(contexts.size() == 0) {
                            //noinspection ResultOfMethodCallIgnored
                            contextsSemaphore.tryAcquire(1); //lock semaphore so we wait
                        }
                    }
                }
                catch(InterruptedException ignored) { }
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
    @Override
    public @NotNull Future<PathResult> queueOperation(@NotNull PathOperation operation) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

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

    @Override
    public boolean isAsync() {
        return true;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        synchronized (contexts) {
            for(int i = contexts.size() - 1; i > -1; i--) {
                EngineContext context = contexts.get(i);

                if(context.snapshot.getWorld().equals(event.getWorld())) {
                    synchronized (removalQueue) {
                        removalQueue.add(context);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        WorldUnloadEvent.getHandlerList().unregister(this);
        shouldRun = false;

        try{
            pathfinderThread.interrupt();
            pathfinderThread.join();
        }
        catch (InterruptedException ignored) {
            ArenaApi.warning("Interrupted while waiting for pathfinder thread to shut down!");
        }

        disposed = true;
    }
}