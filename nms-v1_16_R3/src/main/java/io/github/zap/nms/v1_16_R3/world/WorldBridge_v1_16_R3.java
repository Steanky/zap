package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.WorldBridge;
import io.github.zap.nms.common.world.WrappedChunkSnapshot;
import io.github.zap.nms.common.world.WrappedVoxelShape;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
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
    public @NotNull WrappedVoxelShape collisionShape(@NotNull World world, @NotNull BlockData data, int x, int y, int z) {
        return new WrappedVoxelShape_v1_16_R3(((CraftBlockData)data).getState()
                .getCollisionShape(((CraftWorld)world).getHandle(), new BlockPosition(x, y, z)));
    }

    @Override
    public @NotNull WrappedChunkSnapshot takeSnapshot(@NotNull Chunk chunk) {
        return new WrappedChunkSnapshot_v1_16_R3(chunk);
    }
}
