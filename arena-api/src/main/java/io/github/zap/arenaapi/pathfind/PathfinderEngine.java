package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import lombok.Value;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PathfinderEngine implements PathfinderContext {
    private static final int COMPLETED_PATH_MAX_AGE = 10;

    @Override
    public List<PathfinderEngine.Entry> ongoingOperations() {
        return operations;
    }

    @Override
    public SnapshotProvider snapshotProvider() {
        return null;
    }

    @Value
    public static class Entry {
        PathOperation operation;
        CompletableFuture<PathResult> future;
    }

    private final Thread pathfinderThread = new Thread(null, this::pathfind, "Pathfinder");
    private final AtomicBoolean canRun = new AtomicBoolean(false);

    private final List<Entry> operations = new ArrayList<>();

    private final Object enqueueLock = new Object();

    public PathfinderEngine(@NotNull World world) {
        pathfinderThread.start();

    }

    private void pathfind() {
        try {
            while(true) {
                try {
                    wait();

                    if(canRun.get()) { //make sure we aren't getting a spurious wakeup
                        int startIndex;
                        synchronized (enqueueLock) {
                            startIndex = operations.size() - 1;
                        }

                        for(int i = startIndex; i > -1; i--) {
                            Entry entry = operations.get(i);

                            for(int j = 0; j < entry.operation.desiredIterations(); j++) {
                                if(entry.operation.step(this)) {
                                    if(entry.operation.incrementAge() == COMPLETED_PATH_MAX_AGE) {
                                        operations.remove(i);
                                    }

                                    break;
                                }
                            }
                        }

                        canRun.set(false);
                    }
                }
                catch (InterruptedException ignore) {
                    ArenaApi.warning("Pathfinder thread got interrupted!");
                }
            }
        }
        catch (Exception exception) {
            ArenaApi.severe("Fatal exception in pathfinding thread: " + exception.getMessage());
        }
    }

    /**
     * Queues a pathfinding operation onto the pathfinding thread.
     * @param operation
     */
    public Future<PathResult> queueOperation(PathOperation operation) {
        CompletableFuture<PathResult> future = new CompletableFuture<>();
        synchronized (enqueueLock) {
            operations.add(new Entry(operation, future));
        }

        pathfinderThread.notify();
        return future;
    }
}
