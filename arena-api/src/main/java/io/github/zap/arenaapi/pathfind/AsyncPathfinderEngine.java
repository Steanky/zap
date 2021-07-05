package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class AsyncPathfinderEngine implements PathfinderEngine, Listener {
    private static final AsyncPathfinderEngine INSTANCE = new AsyncPathfinderEngine();
    private static final int MAX_AGE_BEFORE_UPDATE = 40;
    private static final long SYNC_TIMEOUT = 10L;

    private static class Entry {
        private final PathOperation operation;
        private final Consumer<PathResult> consumer;
        private int lastSync = -1;

        private Entry(@NotNull PathOperation operation, @NotNull Consumer<PathResult> consumer) {
            this.operation = Objects.requireNonNull(operation, "operation cannot be null!");
            this.consumer = consumer;
        }
    }

    private class Context implements PathfinderContext {
        private static int totalIndex;

        private final int index;

        private final Semaphore semaphore = new Semaphore(0);

        private final BlockingQueue<Entry> entries = new ArrayBlockingQueue<>(2048);
        private final List<PathResult> successfulPaths = new ArrayList<>();
        private final List<PathResult> failedPaths = new ArrayList<>();
        private final BlockCollisionProvider blockCollisionProvider;

        private int lastSync = -1;

        private Context(@NotNull BlockCollisionProvider blockCollisionProvider) {
            this.blockCollisionProvider = blockCollisionProvider;
            index = totalIndex++;
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
        public @NotNull BlockCollisionProvider blockProvider() {
            return blockCollisionProvider;
        }
    }

    private final ExecutorCompletionService<Context> completionService =
            new ExecutorCompletionService<>(Executors.newCachedThreadPool());

    private final BlockingQueue<Context> contexts = new ArrayBlockingQueue<>(128);

    //inside joke, there's no good reason this should be a treemap
    private final TreeMap<Context, Context> removalQueue = new TreeMap<>(Comparator.comparingInt(o -> o.index));

    private AsyncPathfinderEngine() { //singleton: bad idea to create more than once instance
        Thread pathfinderThread = new Thread(this::pathfind, "Pathfinder");
        pathfinderThread.start();

        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private void pathfind() {
        while(true) {
            try {
                try {
                    //each EngineContext object = different world
                    int operations = 0;

                    for (Context context : contexts) {
                        //don't try to update snapshots too fast
                        boolean skipSync = context.lastSync != -1 && Bukkit.getCurrentTick() - context.lastSync < MAX_AGE_BEFORE_UPDATE;

                        AtomicBoolean syncRun = null;
                        int syncId = -1;

                        if (!skipSync) {
                            //syncing must block main thread
                            syncRun = new AtomicBoolean(false);

                            AtomicBoolean finalSyncRun = syncRun;
                            syncId = Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                                if (!finalSyncRun.getAndSet(true)) {
                                    context.lastSync = Bukkit.getCurrentTick();

                                    for (Entry entry : context.entries) {
                                        if (entry.lastSync == -1 || Bukkit.getCurrentTick() - entry.lastSync > MAX_AGE_BEFORE_UPDATE) {
                                            entry.lastSync = Bukkit.getCurrentTick();
                                            context.blockCollisionProvider.updateRegion(entry.operation.searchArea());
                                        }
                                    }

                                    context.semaphore.release();
                                }
                            }).getTaskId();
                        }

                        if (skipSync || context.semaphore.tryAcquire(SYNC_TIMEOUT, TimeUnit.SECONDS)) {
                            try {
                                completionService.submit(() -> processContext(context));
                                operations++;
                            } catch (RejectedExecutionException exception) {
                                ArenaApi.warning("Unable to queue pathfinding operation(s) for world " + context.blockCollisionProvider.world());
                            }
                        } else if (!syncRun.getAndSet(true)) {
                            ArenaApi.warning("Timed out while waiting on main thread to sync chunks.");
                            Bukkit.getScheduler().cancelTask(syncId);
                        }
                    }

                    //wait for all of the operations we just queued
                    for(int i = 0; i < operations; i++) {
                        try {
                            completionService.take().get();
                        }
                        catch (InterruptedException exception) {
                            ArenaApi.warning("Worker task was interrupted: " + exception.getMessage());
                        }
                    }

                    //clean up contexts that may have been removed, such as by a world unload
                    synchronized (removalQueue) {
                        for(Context context : removalQueue.values()) {
                            context.blockCollisionProvider.clearOwned();
                            contexts.remove(context);
                        }

                        removalQueue.clear();
                    }
                }
                catch(InterruptedException ignored) {
                    ArenaApi.warning("Pathfinder thread was interrupted.");
                    break;
                }
            }
            catch (Exception exception) {
                ArenaApi.warning("Unhandled exception occurred in pathfinding thread: ");
                exception.printStackTrace();
                ArenaApi.warning("Clearing state and continuing.");

                contexts.clear();
            }
        }
    }

    private Context processContext(Context context) throws InterruptedException {
        while (true) { //iterate all context operations until they are complete
            Iterator<Entry> entryIterator = context.entries.iterator();
            while (entryIterator.hasNext()) { //iterate all pathfinding operations for this world
                boolean entryRemoved = false;
                try {
                    Entry entry = entryIterator.next();

                    if(entry != null) {
                        PathOperation operation = entry.operation;

                        if(operation.state() == PathOperation.State.NOT_STARTED) {
                            operation.init(context);
                        }

                        if(operation.state() == PathOperation.State.STARTED) {
                            for (int j = 0; j < operation.iterations(); j++) {
                                operation_step:
                                if (operation.step(context)) {
                                    PathResult result = operation.result();

                                    entryIterator.remove();
                                    entryRemoved = true;

                                    switch (operation.state()) {
                                        case SUCCEEDED -> context.successfulPaths.add(result);
                                        case FAILED -> context.failedPaths.add(result);
                                        default -> {
                                            ArenaApi.warning("PathOperation " + operation + " has an invalid " +
                                                    "state: should be either SUCCEEDED or FAILED. Consumer will not be called.");
                                            break operation_step;
                                        }
                                    }

                                    entry.consumer.accept(result);
                                    break;
                                }

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }
                            }
                        }
                    }
                }
                catch (Exception exception) {
                    if(!(exception instanceof InterruptedException)) {
                        ArenaApi.warning("A PathOperation threw an unhandled exception.");
                        ArenaApi.warning("Context: " + context);
                        ArenaApi.warning("Stack trace: ");
                        exception.printStackTrace();
                    }

                    if(!entryRemoved) {
                        context.entries.remove();
                    }
                }
            }

            if (context.entries.isEmpty()) {
                context.failedPaths.clear();
                context.successfulPaths.clear();
                return context;
            }
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread. This method is thread-safe.
     * @param operation The operation to enqueue
     */
    @Override
    public synchronized void giveOperation(@NotNull PathOperation operation, @NotNull World world,
                                           @NotNull Consumer<PathResult> resultConsumer) {
        Objects.requireNonNull(operation, "operation cannot be null!");
        Objects.requireNonNull(world, "world cannot be null!");
        Objects.requireNonNull(resultConsumer, "resultConsumer cannot be null!");

        Context targetContext = null;
        for(Context context : contexts) {
            if(context.blockProvider().world().getUID().equals(world.getUID())) {
                targetContext = context;
                break;
            }
        }

        if(targetContext == null) {
            targetContext = new Context(new AsyncBlockCollisionProvider(world, MAX_AGE_BEFORE_UPDATE));
            targetContext.entries.add(new Entry(operation, resultConsumer));

            contexts.add(targetContext);
        }
        else {
            targetContext.entries.add(new Entry(operation, resultConsumer));
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        for(Context context : contexts) {
            if(context.blockCollisionProvider.world().getUID().equals(event.getWorld().getUID())) {
                synchronized (removalQueue) {
                    removalQueue.put(context, context);
                }
            }
        }
    }

    public static PathfinderEngine instance() {
        return INSTANCE;
    }
}