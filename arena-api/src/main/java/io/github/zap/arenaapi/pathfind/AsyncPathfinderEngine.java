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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class AsyncPathfinderEngine implements PathfinderEngine, Listener {
    private static final AsyncPathfinderEngine INSTANCE = new AsyncPathfinderEngine();

    private static final long SYNC_TIMEOUT = 5L;

    private static class Entry {
        private final PathOperation operation;
        private final Consumer<PathResult> consumer;

        private Entry(@NotNull PathOperation operation, @NotNull Consumer<PathResult> consumer) {
            this.operation = Objects.requireNonNull(operation, "operation cannot be null!");
            this.consumer = consumer;
        }
    }

    private class Context implements PathfinderContext {
        //don't lock onto context object itself as it is visible to implementations

        private final Object lockHandle = new Object();

        private final Semaphore semaphore = new Semaphore(0);

        private final List<Entry> entries = new ArrayList<>();
        private final List<PathResult> successfulPaths = new ArrayList<>();
        private final List<PathResult> failedPaths = new ArrayList<>();
        private final BlockProvider blockProvider;

        private Context(@NotNull BlockProvider blockProvider) {
            this.blockProvider = blockProvider;
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
        public @NotNull BlockProvider blockProvider() {
            return blockProvider;
        }

        @Override
        public String toString() {
            return "Context{" + "}";
        }
    }

    private final Thread pathfinderThread;
    private final ExecutorService pathWorker = Executors.newCachedThreadPool();
    private final ExecutorCompletionService<Context> completionService = new ExecutorCompletionService<>(pathWorker);
    private final List<Context> contexts = new ArrayList<>();
    private final Semaphore contextsSemaphore = new Semaphore(0);
    private final Queue<Context> removalQueue = new ArrayDeque<>();

    private boolean disposed = false;

    private final Object completedLockHandle = new Object();

    private AsyncPathfinderEngine() { //singleton: bad idea to create more than once instance
        pathfinderThread = new Thread(this::pathfind, "Pathfinder");
        pathfinderThread.start();

        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private void pathfind() {
        while(true) {
            try {
                try {
                    contextsSemaphore.acquire();

                    int contextStartingIndex;
                    synchronized (contexts) {
                        contextStartingIndex = contexts.size() - 1;
                    }

                    //each EngineContext object = different world
                    int operations = 0;
                    for(int i = contextStartingIndex; i > -1; i--) {
                        Context context = contexts.get(i);

                        //syncing must be run on main thread
                        AtomicBoolean syncRun = new AtomicBoolean(false);
                        int syncId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                            if(!syncRun.getAndSet(true)) {
                                context.blockProvider.updateAll();
                                context.semaphore.release();
                            }
                        }).getTaskId();

                        if(context.semaphore.tryAcquire(SYNC_TIMEOUT, TimeUnit.SECONDS)) {
                            try {
                                completionService.submit(() -> calculatePathsFor(context));
                                operations++;
                            }
                            catch (RejectedExecutionException exception) {
                                ArenaApi.warning("Unable to queue pathfinding operation(s) for world " + context.blockProvider.getWorld());
                            }
                        }
                        else {
                            if(!syncRun.getAndSet(true)) {
                                ArenaApi.warning("Timed out while waiting on main thread to sync chunks.");
                                Bukkit.getScheduler().cancelTask(syncId);
                            }
                        }
                    }

                    //wait for all of the operations we just queued
                    List<Context> completedContexts = new ArrayList<>();
                    for(int i = 0; i < operations; i++) {
                        try {
                            completedContexts.add(completionService.take().get());
                        }
                        catch (InterruptedException exception) {
                            ArenaApi.warning("Worker task was interrupted: " + exception.getMessage());
                        }
                    }

                    //check the operations on each context, if none have pending operations, lock again
                    synchronized (completedLockHandle) {
                        for(Context completed : completedContexts) {
                            if(completed.entries.size() > 0) {
                                break;
                            }
                        }

                        //noinspection ResultOfMethodCallIgnored
                        contextsSemaphore.tryAcquire(1);
                    }

                    synchronized (contexts) {
                        while(!removalQueue.isEmpty()) {
                            contexts.remove(removalQueue.remove());
                        }

                        if(contexts.size() == 0) { //lock semaphore, we have no contexts
                            //noinspection ResultOfMethodCallIgnored
                            contextsSemaphore.tryAcquire(1);
                        }
                    }

                    //noinspection BusyWait
                    Thread.sleep(400);
                }
                catch(InterruptedException ignored) {
                    break;
                }
            }
            catch (Exception exception) {
                ArenaApi.warning("Unhandled exception occurred in pathfinding thread: ");
                exception.printStackTrace();
                ArenaApi.warning("Clearing state and continuing.");

                synchronized (contexts) {
                    contexts.clear();
                    //noinspection ResultOfMethodCallIgnored
                    contextsSemaphore.tryAcquire(1); //lock since we know we're empty
                }
            }
        }
    }

    private Context calculatePathsFor(Context context) throws InterruptedException {
        while (true) { //iterate all context operations until they are complete
            int operationStartingIndex;
            synchronized (context.lockHandle) {
                operationStartingIndex = context.entries.size() - 1;
            }

            for (int i = operationStartingIndex; i > -1; i--) { //iterate all pathfinding operations for this world
                try {
                    Entry entry = context.entries.get(i);

                    if(entry.operation.state() == PathOperation.State.NOT_STARTED) {
                        entry.operation.init();
                    }

                    PathOperation.State entryState = entry.operation.state();
                    if (entryState == PathOperation.State.STARTED) {
                        for (int j = 0; j < entry.operation.iterations(); j++) {
                            if (entry.operation.step(context)) {
                                PathResult result = entry.operation.result();

                                if (entry.operation.state() == PathOperation.State.SUCCEEDED) {
                                    context.successfulPaths.add(result);
                                } else if (entry.operation.state() == PathOperation.State.FAILED) {
                                    context.failedPaths.add(result);
                                } else {
                                    ArenaApi.warning("PathOperation " + entry.operation + " has an invalid state: should be " +
                                            "either SUCCEEDED or FAILED");
                                    ArenaApi.warning("Removing invalid PathOperation without calling consumer.");
                                    removeOperation(context, i);
                                    break;
                                }

                                entry.consumer.accept(result);
                                removeOperation(context, i);
                                break;
                            }

                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    if(!(ex instanceof InterruptedException)) {
                        ArenaApi.warning("A PathOperation threw an unhandled exception.");
                        ArenaApi.warning("Context: " + context);
                    }
                }
            }

            synchronized (context.lockHandle) {
                if (context.entries.size() == 0) {
                    context.failedPaths.clear();
                    context.successfulPaths.clear();
                    break; //break if we did all the operations
                }
            }
        }

        return context;
    }

    private void removeOperation(Context context, int index) {
        synchronized (context.lockHandle) {
            context.entries.remove(index);
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     */
    @Override
    public synchronized void giveOperation(@NotNull PathOperation operation, @NotNull World world,
                                           @NotNull Consumer<PathResult> resultConsumer) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        Objects.requireNonNull(operation, "operation cannot be null!");
        Objects.requireNonNull(world, "world cannot be null!");
        Objects.requireNonNull(resultConsumer, "resultConsumer cannot be null!");

        Context targetContext = null;
        synchronized (contexts) {
            for(Context context : contexts) {
                if(context.blockProvider().getWorld().getUID().equals(world.getUID())) {
                    targetContext = context;
                    break;
                }
            }
        }

        if(targetContext == null) {
            targetContext = new Context(new AsyncBlockProvider(world, operation.searchArea()));
            targetContext.entries.add(new Entry(operation, resultConsumer));

            synchronized (contexts) {
                contexts.add(targetContext);
                contextsSemaphore.release();
            }
        }
        else {
            synchronized (targetContext.lockHandle) {
                synchronized (completedLockHandle) {
                    targetContext.entries.add(new Entry(operation, resultConsumer));
                    contextsSemaphore.release();
                }
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
            for(Context context : contexts) {
                if(context.blockProvider.getWorld().equals(event.getWorld())) {
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

        try {
            pathfinderThread.interrupt();
            pathfinderThread.join();
            pathWorker.shutdown();
            if(!pathWorker.awaitTermination(5L, TimeUnit.SECONDS)) {
                ArenaApi.warning("Pathfinder thread successfully shut down, but the worker service did not terminate" +
                        " after 5s.");
                ArenaApi.warning("Attempting to interrupt threads. There may be exception spam.");
                pathWorker.shutdownNow();

                if(!pathWorker.awaitTermination(5L, TimeUnit.SECONDS)) {
                    ArenaApi.warning("Pathfinder worker service was interrupted, but did not terminate after 5 seconds.");
                    ArenaApi.warning("The worker threads may never terminate and could cause lag.");
                }
            }
        }
        catch (InterruptedException ignored) {
            ArenaApi.warning("Interrupted while waiting for pathfinder thread to shut down!");
            ArenaApi.warning("Pathfinder thread state: " + pathfinderThread.getState());
            ArenaApi.warning("It may never terminate and could throw exceptions.");
        }
        finally {
            disposed = true;
        }
    }

    public static PathfinderEngine instance() {
        return INSTANCE;
    }
}