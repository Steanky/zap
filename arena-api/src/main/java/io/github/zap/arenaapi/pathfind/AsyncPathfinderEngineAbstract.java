package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

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

        return completionService.submit(() -> {
            try {
                return processOperation(context, operation);
            }
            catch (Exception exception) {
                ArenaApi.getInstance().getLogger().log(Level.WARNING, "Exception thrown in PathOperation handler", exception);
            }

            return null;
        });
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    protected void preProcess(@NotNull T context, @NotNull PathOperation operation) {}

    protected @Nullable PathResult processOperation(@NotNull T context, @NotNull PathOperation operation) {
        preProcess(context, operation);
        operation.init(context);

        while(operation.state() == PathOperation.State.STARTED) {
            for(int i = 0; i < operation.iterations(); i++) {
                if(operation.step(context)) {
                    PathResult result = operation.result();
                    context.recordPath(result);
                    return result;
                }
            }

            if(Thread.interrupted()) {
                ArenaApi.warning("processOperation interrupted for PathOperation. Returning null PathResult.");
                return null;
            }

            if(operation.allowMerges()) {
                PathResult result = context.merger().attemptMerge(operation, context);

                if(result != null) {
                    return result;
                }
            }
        }

        return operation.result();
    }

    protected abstract @NotNull T makeContext(@NotNull BlockCollisionProvider provider);

    protected abstract @NotNull BlockCollisionProvider getBlockCollisionProvider(@NotNull World world);
}
