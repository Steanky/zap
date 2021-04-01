package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.ObjectDisposedException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncPathfinderEngine implements PathfinderEngine, Listener {
    public static class Entry {
        public final PathOperation operation;
        public final Consumer<PathResult> consumer;

        private Entry(@NotNull PathOperation operation, @NotNull Consumer<PathResult> consumer) {
            this.operation = Objects.requireNonNull(operation, "operation cannot be null!");
            this.consumer = Objects.requireNonNull(consumer,"future cannot be null!");
        }
    }

    private class Context implements PathfinderContext {
        private final Semaphore semaphore = new Semaphore(0);

        private final List<Entry> operations = new ArrayList<>();
        private final List<PathResult> successfulPaths = new ArrayList<>();
        private final List<PathResult> failedPaths = new ArrayList<>();
        private final SnapshotProvider snapshot;

        private Context(@NotNull SnapshotProvider snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public @NotNull PathfinderEngine engine() {
            return AsyncPathfinderEngine.this;
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
    private final List<Context> contexts = new ArrayList<>();
    private final Semaphore contextsSemaphore = new Semaphore(0);
    private final Queue<Context> removalQueue = new ArrayDeque<>();

    private boolean disposed = false;
    private boolean shouldRun = true;

    AsyncPathfinderEngine() {
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
                        Context context = contexts.get(i);

                        BukkitTask syncTask = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> { //syncing must be run on main thread
                            context.snapshot.syncWithWorld();
                            context.semaphore.release();
                        });

                        if(context.semaphore.tryAcquire(5L, TimeUnit.SECONDS)) {
                            //noinspection ResultOfMethodCallIgnored
                            context.semaphore.tryAcquire(1); //reset the semaphore

                            contextLoop:
                            while(true) {
                                int operationStartingIndex;
                                synchronized (context) {
                                    operationStartingIndex = context.operations.size() - 1;
                                }

                                operationLoop:
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
                                                    ArenaApi.warning("PathOperation " + entry.operation + " has an invalid" +
                                                            " state: should be either SUCCEEDED or FAILED, but was " +
                                                            entry.operation.getState().toString());
                                                    ArenaApi.warning("Removing invalid PathOperation.");

                                                    removeOperation(context, j);
                                                    break operationLoop;
                                                }

                                                entry.consumer.accept(result);
                                                break;
                                            }

                                            if(Thread.interrupted()) {
                                                throw new InterruptedException();
                                            }
                                        }
                                    }

                                    if(entryState != PathOperation.State.INCOMPLETE && entry.operation.shouldRemove()) { //remove successful or failed paths if they ask
                                        PathResult entryResult = entry.operation.getResult();

                                        if(entryState == PathOperation.State.SUCCEEDED) {
                                            context.successfulPaths.remove(entryResult);
                                        }
                                        else if(entryState == PathOperation.State.FAILED) {
                                            context.failedPaths.remove(entryResult);
                                        }

                                        if(removeOperation(context, j)) {
                                            break contextLoop;
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            ArenaApi.warning("Timed out while waiting on main thread.");
                            Bukkit.getScheduler().cancelTask(syncTask.getTaskId());
                        }
                    }
                }
                catch(InterruptedException ignored) {
                    if(shouldRun) {
                        ArenaApi.warning("Pathfinder thread was interrupted, but not asked to shut down.");
                    }
                }
                finally {
                    synchronized (contexts) {
                        while(!removalQueue.isEmpty()) {
                            contexts.remove(removalQueue.remove());
                        }

                        if(contexts.size() == 0) { //lock semaphore, we have no context
                            //noinspection ResultOfMethodCallIgnored
                            contextsSemaphore.tryAcquire(1);
                        }
                    }
                }
            }
            catch (Exception exception) {
                ArenaApi.warning("Unhandled exception occurred in pathfinding thread: ");
                ArenaApi.warning(exception.toString());
                ArenaApi.warning("Clearing state and continuing.");

                synchronized (contexts) {
                    contexts.clear();
                    //noinspection ResultOfMethodCallIgnored
                    contextsSemaphore.tryAcquire(1);
                }
            }
        }
    }

    private boolean removeOperation(Context context, int index) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (context) {
            context.operations.remove(index);
            return context.operations.size() == 0;
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     */
    @Override
    public synchronized void giveOperation(@NotNull PathOperation operation, @NotNull World world, @NotNull Consumer<PathResult> resultConsumer) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        Objects.requireNonNull(operation, "operation cannot be null!");
        Objects.requireNonNull(world, "world cannot be null!");
        Objects.requireNonNull(resultConsumer, "resultConsumer cannot be null!");

        Context targetContext = null;
        synchronized (contexts) {
            for(Context context : contexts) {
                if(context.snapshotProvider().getWorld().getUID().equals(world.getUID())) {
                    targetContext = context;
                    break;
                }
            }
        }

        if(targetContext == null) {
            targetContext = new Context(new WorldSnapshotProvider(world));
            targetContext.operations.add(new Entry(operation, resultConsumer));

            synchronized (contexts) {
                contexts.add(targetContext);
                contextsSemaphore.release();
            }
        }
        else {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (targetContext) {
                targetContext.operations.add(new Entry(operation, resultConsumer));
                contextsSemaphore.release();
            }
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        synchronized (contexts) {
            for(int i = contexts.size() - 1; i > -1; i--) {
                Context context = contexts.get(i);

                if(context.snapshot.getWorld().equals(event.getWorld())) {
                    removalQueue.add(context);
                }
            }
        }
    }

    @Override
    public synchronized void dispose() {
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
            ArenaApi.warning("Thread state: " + pathfinderThread.getState());
            ArenaApi.warning("The thread may never terminate.");
        }
        finally {
            disposed = true;
        }
    }
}