package io.github.zap.arenaapi.v1_17_R1.world;

import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldBridge_v1_17_R1 implements WorldBridge {
    public static final WorldBridge_v1_17_R1 INSTANCE = new WorldBridge_v1_17_R1();

    private WorldBridge_v1_17_R1() {}

    @Override
    public @NotNull String getDefaultWorldName() {
        return (((CraftServer) Bukkit.getServer()).getServer()).getProperties().levelName;
    }

    @Override
    public @NotNull CollisionChunkView proxyView(@NotNull Chunk chunk) {
        return new CollisionChunkProxy_v1_17_R1(((CraftChunk)chunk).getHandle());
    }

    @Override
    public @NotNull CollisionChunkView snapshotView(@NotNull Chunk chunk) {
        return new CollisionChunkSnapshot_v1_17_R1(((CraftChunk)chunk).getHandle());
    }

    @Override
    public boolean blockHasCollision(@NotNull Block block) {
        return !((CraftBlockState)block.getState()).getHandle().getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                new BlockPos(block.getX(), block.getY(), block.getZ())).isEmpty();
    }

    @Override
    public @NotNull VoxelShapeWrapper collisionShapeFor(@NotNull Block block) {
        return new VoxelShapeWrapper_v1_17_R1(((CraftBlockState)block.getState()).getHandle()
                .getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                        new BlockPos(block.getX(), block.getY(), block.getZ())));
    }

    @Override
    public @Nullable Chunk getChunkIfLoadedImmediately(@NotNull World world, int x, int z) {
        CraftWorld craftWorld = (CraftWorld) world;
        LevelChunk chunk = craftWorld.getHandle().getChunkSource().getChunkAtIfLoadedImmediately(x, z);
        return chunk == null ? null : chunk.bukkitChunk;
    }
}
