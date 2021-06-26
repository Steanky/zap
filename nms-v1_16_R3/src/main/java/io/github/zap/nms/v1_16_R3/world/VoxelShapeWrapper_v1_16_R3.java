package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BoxConsumer;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.VoxelShape;
import net.minecraft.server.v1_16_R3.VoxelShapes;
import org.jetbrains.annotations.NotNull;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    private final VoxelShape shape;

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
    }

    @Override
    public void forEachBox(@NotNull BoxConsumer consumer) {
        shape.b(consumer::consume);
    }

    @Override
    public double maxY() {
        return shape.c(EnumDirection.EnumAxis.Y);
    }

    @Override
    public double minY() {
        return shape.b(EnumDirection.EnumAxis.Y);
    }

    @Override
    public boolean isFull() {
        return shape == VoxelShapes.fullCube();
    }

    @Override
    public boolean isEmpty() {
        return shape == VoxelShapes.empty();
    }

    public @NotNull VoxelShape getShape() {
        return shape;
    }
}