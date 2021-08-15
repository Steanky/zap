package io.github.zap.arenaapi.pathfind;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

abstract class AsyncPathfinderEngineAbstract implements PathfinderEngine {
    private final ExecutorCompletionService<PathResult> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
}
