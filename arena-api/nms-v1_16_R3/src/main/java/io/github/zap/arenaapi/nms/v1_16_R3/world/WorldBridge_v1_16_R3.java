package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class WorldBridge_v1_16_R3 implements WorldBridge {
    public static final WorldBridge_v1_16_R3 INSTANCE = new WorldBridge_v1_16_R3();

    private WorldBridge_v1_16_R3() {}

    @Override
    public @NotNull String getDefaultWorldName() {
        return ((((CraftServer) Bukkit.getServer()).getServer()).getDedicatedServerProperties()).levelName;
    }

    @Override
    public @NotNull CollisionChunkView proxyView(@NotNull Chunk chunk, int expectedConcurrency, long timeoutInterval,
                                                 @NotNull TimeUnit timeoutUnit) {
        return new CollisionChunkProxy_v1_16_R3(((CraftChunk)chunk).getHandle(), expectedConcurrency, timeoutInterval, timeoutUnit);
    }

    @Override
    public @NotNull CollisionChunkView snapshotView(@NotNull Chunk chunk) {
        return new CollisionChunkSnapshot_v1_16_R3(((CraftChunk)chunk).getHandle());
    }

    @Override
    public boolean blockHasCollision(@NotNull Block block) {
        return !((CraftBlockState)block.getState()).getHandle().getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                new BlockPosition(block.getX(), block.getY(), block.getZ())).isEmpty();
    }

    @Override
    public @NotNull BlockCollisionView collisionFor(@NotNull Block block) {
        return BlockCollisionView.from(block.getX(), block.getY(), block.getZ(), block.getBlockData(),
                new VoxelShapeWrapper_v1_16_R3(((CraftBlockState)block.getState()).getHandle()
                        .getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                        new BlockPosition(block.getX(), block.getY(), block.getZ()))));
    }

    @Override
    public @Nullable Chunk getChunkIfLoadedImmediately(@NotNull World world, int x, int z) {
        CraftWorld craftWorld = (CraftWorld) world;
        net.minecraft.server.v1_16_R3.Chunk chunk = craftWorld.getHandle().getChunkProvider().getChunkAtIfLoadedImmediately(x, z);
        return chunk == null ? null : chunk.bukkitChunk;
    }
}
