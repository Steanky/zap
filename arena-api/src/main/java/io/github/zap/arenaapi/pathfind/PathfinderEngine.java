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
    private static final int COMPLETED_PATH_MAX_AGE = 1000;

    public static class Entry {
        public final PathOperation operation;
        public final CompletableFuture<PathResult> future;

        private Entry(PathOperation operation, CompletableFuture<PathResult> future) {
            this.operation = operation;
            this.future = future;
        }
    }

    public static class EngineContext implements PathfinderContext {
        private final Object waitHandle = new Object();

        private final List<Entry> operations = new ArrayList<>();
        private final Set<PathResult> successfulPaths = new HashSet<>();
        private final Set<PathResult> failedPaths = new HashSet<>();
        private final SnapshotProvider provider;

        public EngineContext(@NotNull SnapshotProvider provider) {
            this.provider = provider;
        }

        @Override
        public @NotNull List<Entry> ongoingOperations() {
            return operations;
        }

        @Override
        public @NotNull Set<PathResult> successfulPaths() {
            return successfulPaths;
        }

        @Override
        public @NotNull Set<PathResult> failedPaths() {
            return failedPaths;
        }

        @Override
        public @NotNull SnapshotProvider snapshotProvider() {
            return provider;
        }
    }

    private final Thread pathfinderThread = new Thread(null, this::pathfind, "Pathfinder");

    private final List<EngineContext> contexts = new ArrayList<>();

    public PathfinderEngine() {
        pathfinderThread.start();
        Bukkit.getServer().getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private void pathfind() {
        try {
            while(true) {
                try {
                    pathfinderThread.wait();

                    int contextStartingIndex;
                    synchronized (contexts) {
                        contextStartingIndex = contexts.size() - 1;
                    }

                    for(int i = contextStartingIndex; i > -1; i--) {
                        EngineContext context = contexts.get(i);

                        AtomicBoolean completed = new AtomicBoolean(false);

                        Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                            context.provider.syncWithWorld();
                            completed.set(true);
                            context.waitHandle.notify();
                        });

                        while(!completed.get()) {
                            try {
                                context.waitHandle.wait();
                            }
                            catch (InterruptedException ignored) {}
                        }

                        int operationStartingIndex;

                        synchronized (context) {
                            operationStartingIndex = context.operations.size() - 1;
                        }

                        for(int j = operationStartingIndex; j > -1; j--) {
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
                                            throw new IllegalStateException("Path said it completed, but state == INCOMPLETE!");
                                        }

                                        entry.future.complete(result);
                                    }
                                }
                            }
                            else if(entry.operation.incrementAge() == COMPLETED_PATH_MAX_AGE) {
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
                }
                catch (InterruptedException interruptedException) {
                    ArenaApi.warning("Pathfinder thread was interrupted!");
                }
            }
        }
        catch (Exception exception) {
            ArenaApi.severe("Fatal exception in pathfinding thread: " + exception.getMessage());
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread.
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
            }
        }
        else {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (targetContext) {
                targetContext.operations.add(new Entry(operation, future));
            }
        }

        pathfinderThread.notify();
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
