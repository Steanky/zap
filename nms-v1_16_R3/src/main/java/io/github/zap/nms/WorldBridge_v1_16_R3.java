package io.github.zap.nms;

import io.github.zap.nms.world.WorldBridge;
import io.github.zap.nms.world.WrappedVoxelShape;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
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
    public @NotNull WrappedVoxelShape collisionShape(@NotNull Block block) {
        return new WrappedVoxelShape_v1_16_R3(((CraftBlockData) block.getBlockData()).getState()
                .getCollisionShape(((CraftWorld)block.getWorld()).getHandle(),
                        new BlockPosition(block.getX(), block.getY(), block.getZ())));
    }
}
