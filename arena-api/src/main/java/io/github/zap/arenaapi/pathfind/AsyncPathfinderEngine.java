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
    private static final int MAX_ITERATIONS = 10000;

    private static class Entry {
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
        private final BlockProvider provider;

        private Context(@NotNull BlockProvider provider) {
            this.provider = provider;
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
            return provider;
        }
    }

    private final Thread pathfinderThread;
    private final ExecutorService pathWorker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorCompletionService<Context> completionService = new ExecutorCompletionService<>(pathWorker);
    private final List<Context> contexts = new ArrayList<>();
    private final Semaphore contextsSemaphore = new Semaphore(0);
    private final Queue<Context> removalQueue = new ArrayDeque<>();

    private boolean disposed = false;
    private boolean shouldRun = true;

    private final Object completedWaitLock = new Object();

    private AsyncPathfinderEngine() { //singleton: bad idea to create more than once instance
        pathfinderThread = new Thread(null, this::pathfind, "Pathfinder");
        pathfinderThread.start();

        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private void pathfind() {
        while(shouldRun) {
            try {
                try {
                    contextsSemaphore.acquire();

                    int contextStartingIndex;
                    synchronized (contexts) {
                        contextStartingIndex = contexts.size() - 1;
                    }

                    int ops = 0;
                    for(int i = contextStartingIndex; i > -1; i--) { //each EngineContext object = different world
                        Context context = contexts.get(i);

                        AtomicBoolean syncRun = new AtomicBoolean(false);
                        BukkitTask syncTask = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> { //syncing must be run on main thread
                            if(!syncRun.getAndSet(true)) {
                                context.provider.updateAll();
                                context.semaphore.release();
                            }
                        });

                        if(context.semaphore.tryAcquire(SYNC_TIMEOUT, TimeUnit.SECONDS)) {
                            ArenaApi.info("World synchronization semaphore acquired.");
                            //noinspection ResultOfMethodCallIgnored
                            context.semaphore.tryAcquire(1); //reset the semaphore

                            try {
                                completionService.submit(() -> calculatePathsFor(context));
                                ops++;
                            }
                            catch (RejectedExecutionException exception) {
                                ArenaApi.warning("Unable to queue pathfinding operation(s) for world " + context.provider.getWorld());
                            }
                        }
                        else {
                            if(!syncRun.getAndSet(true)) {
                                ArenaApi.warning("Timed out while waiting on main thread.");
                                Bukkit.getScheduler().cancelTask(syncTask.getTaskId());
                            }
                        }
                    }

                    //wait for all of the operations we just queued
                    List<Context> completedContexts = new ArrayList<>();
                    for(int i = 0; i < ops; i++) {
                        Future<Context> contextFuture = completionService.take();
                        completedContexts.add(contextFuture.get());
                    }

                    //check the operations on each context, if none have pending operations, ignore
                    synchronized (completedWaitLock) {
                        for(Context completed : completedContexts) {
                            if(completed.operations.size() > 0) {
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
                }
                catch(InterruptedException ignored) {
                    if(shouldRun) {
                        ArenaApi.warning("Pathfinder thread was interrupted, but not asked to shut down.");
                    }
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
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (context) {
                operationStartingIndex = context.operations.size() - 1;
            }

            synchronized (context.operations) {
                for (int j = operationStartingIndex; j > -1; j--) { //iterate all pathfinding operations for this world
                    Entry entry = context.operations.get(j);

                    PathOperation.State entryState = entry.operation.getState();
                    if (entryState == PathOperation.State.INCOMPLETE) {
                        for (int k = 0; k < Math.min(entry.operation.desiredIterations(), MAX_ITERATIONS); k++) {
                            if (entry.operation.step(context)) {
                                PathResult result = entry.operation.getResult();

                                if (entry.operation.getState() == PathOperation.State.SUCCEEDED) {
                                    context.successfulPaths.add(result);
                                } else if (entry.operation.getState() == PathOperation.State.FAILED) {
                                    context.failedPaths.add(result);
                                } else {
                                    ArenaApi.warning("PathOperation " + entry.operation + " has an invalid" +
                                            " state: should be either SUCCEEDED or FAILED, but was " +
                                            entry.operation.getState().toString());
                                    ArenaApi.warning("Removing invalid PathOperation without calling consumer.");

                                    removeOperation(context, j);
                                    break;
                                }

                                entry.consumer.accept(result);
                                break;
                            }

                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    }

                    //remove successful or failed paths if they ask
                    if (entryState != PathOperation.State.INCOMPLETE && entry.operation.shouldRemove()) {
                        PathResult entryResult = entry.operation.getResult();

                        if (entryState == PathOperation.State.SUCCEEDED) {
                            context.successfulPaths.remove(entryResult);
                        } else if (entryState == PathOperation.State.FAILED) {
                            context.failedPaths.remove(entryResult);
                        }

                        removeOperation(context, j);
                    }
                }
            }

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (context) {
                if (context.operations.size() == 0) {
                    break; //break if we did all the operations
                }
            }
        }

        return context;
    }

    private void removeOperation(Context context, int index) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (context) {
            context.operations.remove(index);
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
                if(context.blockProvider().getWorld().getUID().equals(world.getUID())) {
                    targetContext = context;
                    break;
                }
            }
        }

        if(targetContext == null) {
            targetContext = new Context(new AsyncBlockProvider(world, operation.searchArea()));
            targetContext.operations.add(new Entry(operation, resultConsumer));

            synchronized (contexts) {
                contexts.add(targetContext);
                contextsSemaphore.release();
            }
        }
        else {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (targetContext) {
                synchronized (completedWaitLock) {
                    targetContext.operations.add(new Entry(operation, resultConsumer));
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
                if(context.provider.getWorld().equals(event.getWorld())) {
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
            pathWorker.shutdown();
            if(!pathWorker.awaitTermination(20L, TimeUnit.SECONDS)) {
                ArenaApi.warning("Pathfinder thread successfully shut down, but the worker service did not terminate after 20s.");
                ArenaApi.warning("Pathfinding tasks may be completed after this object has been disposed.");
            }
        }
        catch (InterruptedException ignored) {
            ArenaApi.warning("Interrupted while waiting for pathfinder thread to shut down!");
            ArenaApi.warning("Thread state: " + pathfinderThread.getState());
            ArenaApi.warning("The thread may never terminate and could throw exceptions.");
        }
        finally {
            disposed = true;
        }
    }

    public static PathfinderEngine instance() {
        return INSTANCE;
    }
}