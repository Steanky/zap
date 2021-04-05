package io.github.zap.nms;

import io.github.zap.nms.world.WorldBridge;
import io.github.zap.nms.world.WrappedChunkSnapshot;
import io.github.zap.nms.world.WrappedVoxelShape;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;

class WorldBridge_v1_16_R3 implements WorldBridge {
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
