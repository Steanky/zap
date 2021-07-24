package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.nms.common.world.WorldBridge;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;

public class WorldBridge_v1_16_R3 implements WorldBridge {
    public static final WorldBridge_v1_16_R3 INSTANCE = new WorldBridge_v1_16_R3();

    private WorldBridge_v1_16_R3() {}

    @Override
    public @NotNull String getDefaultWorldName() {
        return ((((CraftServer) Bukkit.getServer()).getServer()).getDedicatedServerProperties()).levelName;
    }

    @Override
    public @NotNull CollisionChunkSnapshot takeSnapshot(@NotNull Chunk chunk) {
        return new CollisionChunkSnapshot_v1_16_R3(chunk);
    }

    @Override
    public boolean blockHasCollision(@NotNull Block block) {
        return !((CraftBlockState)block.getState()).getHandle().getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                new BlockPosition(block.getX(), block.getY(), block.getZ())).isEmpty();
    }

    @Override
    public @NotNull VoxelShapeWrapper collisionShapeFor(@NotNull Block block) {
        return new VoxelShapeWrapper_v1_16_R3(((CraftBlockState)block.getState()).getHandle()
                .getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                new BlockPosition(block.getX(), block.getY(), block.getZ())));
    }
}
