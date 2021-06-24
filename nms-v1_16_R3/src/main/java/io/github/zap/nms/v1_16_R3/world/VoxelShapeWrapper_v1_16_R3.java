package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BoxConsumer;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.VoxelShape;
import net.minecraft.server.v1_16_R3.VoxelShapes;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper_v1_16_R3 FULL_BLOCK = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.fullCube());

    private final VoxelShape shape;

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
    }

    @Override
    public void forEachBox(@NotNull BoxConsumer consumer) {
        shape.b(consumer::consume);
    }

    @Override
    public @NotNull RayTraceResult raycast(@NotNull Vector start, @NotNull Vector end, @NotNull BlockVector pos) {
        return null;
    }

    @Override
    public double maxHeight() {
        return shape.c(EnumDirection.EnumAxis.Y);
    }
}