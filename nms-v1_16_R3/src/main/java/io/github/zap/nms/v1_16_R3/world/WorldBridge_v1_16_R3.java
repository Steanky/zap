package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.WorldBridge;
import io.github.zap.nms.common.world.CollisionChunkSnapshot;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.VoxelShape;
import net.minecraft.server.v1_16_R3.VoxelShapes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
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
        return !((CraftBlockData) block).getState().getCollisionShape(((CraftChunk)block.getChunk()).getHandle(),
                new BlockPosition(block.getX(), block.getY(), block.getZ())).isEmpty();
    }

    @Override
    public boolean isValidChunkCoordinate(int x, int y, int z) {
        return (0 <= x && x <= 15) && (0 <= y && y <= 255) && (0 <= z && z <= 15);
    }

    @Override
    public int minimumY() {
        return 0;
    }

    @Override
    public int maximumY() {
        return 255;
    }
}
