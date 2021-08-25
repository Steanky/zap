package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class AsyncPathfinderEngineAbstract<T extends PathfinderContext> implements PathfinderEngine, Listener {
    protected static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    protected static final ExecutorService pathfindService = Executors.newFixedThreadPool(MAX_THREADS);

    private final Map<UUID, T> contexts;
    protected final Plugin plugin;

    AsyncPathfinderEngineAbstract(@NotNull Map<UUID, T> contexts, @NotNull Plugin plugin) {
        this.contexts = contexts;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        T context = contexts.computeIfAbsent(world.getUID(), (key) -> makeContext(makeBlockCollisionProvider(world)));

        return pathfindService.submit(() -> {
            try {
                return processOperation(context, operation);
            }
            catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Exception thrown in PathOperation handler", exception);
            }

            return null;
        });
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    protected void preProcess(@NotNull T context, @NotNull PathOperation operation) {}

    protected @Nullable PathResult processOperation(@NotNull T context, @NotNull PathOperation operation) {
        preProcess(context, operation);

        while(operation.state() == PathOperation.State.STARTED) {
            for(int i = 0; i < operation.iterations(); i++) {
                if(operation.step(context)) {
                    PathResult result = operation.result();
                    context.recordPath(result);
                    return result;
                }
            }

            if(Thread.interrupted()) {
                plugin.getLogger().log(Level.WARNING, "processOperation interrupted for PathOperation. Returning null PathResult");
                return null;
            }
        }

        return operation.result();
    }

    protected abstract @NotNull T makeContext(@NotNull BlockCollisionProvider provider);

    protected abstract @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world);

    @EventHandler
    private void onWorldUnload(@NotNull WorldUnloadEvent event) {
        PathfinderContext context = contexts.remove(event.getWorld().getUID());

        if(context != null) {
            context.blockProvider().clearForWorld();
            plugin.getLogger().info("Pathfinding context for world " + event.getWorld().getName() + " unloaded");
        }
    }
}
