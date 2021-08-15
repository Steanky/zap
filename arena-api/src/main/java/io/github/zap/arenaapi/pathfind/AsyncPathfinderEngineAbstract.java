package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

abstract class AsyncPathfinderEngineAbstract<T extends PathfinderContext> implements PathfinderEngine {
    protected static final ExecutorCompletionService<PathResult> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

    protected final Map<UUID, T> contexts;

    AsyncPathfinderEngineAbstract(@NotNull Map<UUID, T> contexts) {
        this.contexts = contexts;
    }

    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        T context = contexts.computeIfAbsent(world.getUID(), (key) -> makeContext(getBlockCollisionProvider(world)));
        return completionService.submit(() -> processOperation(context, operation));
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    protected abstract @Nullable PathResult processOperation(@NotNull T context, @NotNull PathOperation operation);

    protected abstract @NotNull T makeContext(@NotNull BlockCollisionProvider provider);

    protected abstract @NotNull BlockCollisionProvider getBlockCollisionProvider(@NotNull World world);
}
