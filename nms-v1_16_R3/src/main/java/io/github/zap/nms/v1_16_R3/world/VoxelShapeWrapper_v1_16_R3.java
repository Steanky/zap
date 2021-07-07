package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.BoxConsumer;
import io.github.zap.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.util.MathUtils;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.VoxelShape;
import net.minecraft.server.v1_16_R3.VoxelShapes;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    private final VoxelShape shape;
    private List<AxisAlignedBB> cachedBounds;
    private final double epsilon = Vector.getEpsilon();

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
        cachedBounds = null;
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

    @Override
    public @NotNull List<BoundingBox> boundingBoxes() {
        List<BoundingBox> bounds = new ArrayList<>();
        for(AxisAlignedBB bb : getCachedBounds()) {
            bounds.add(new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxX));
        }

        return bounds;
    }

    @Override
    public boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for(AxisAlignedBB bounds : getCachedBounds()) {
            if(MathUtils.fuzzyComparison(bounds.minX, maxX, MathUtils.Comparison.LESS_THAN) &&
                    MathUtils.fuzzyComparison(bounds.maxX, minX, MathUtils.Comparison.GREATER_THAN) &&
                    MathUtils.fuzzyComparison(bounds.minY, maxY, MathUtils.Comparison.LESS_THAN) &&
                    MathUtils.fuzzyComparison(bounds.maxY, minY, MathUtils.Comparison.GREATER_THAN) &&
                    MathUtils.fuzzyComparison(bounds.minZ, maxZ, MathUtils.Comparison.LESS_THAN) &&
                    MathUtils.fuzzyComparison(bounds.maxZ, minZ, MathUtils.Comparison.GREATER_THAN)) {
                return true;
            }
        }

        return false;
    }

    public @NotNull VoxelShape getShape() {
        return shape;
    }

    private List<AxisAlignedBB> getCachedBounds() {
        if(cachedBounds == null) {
            cachedBounds = shape.d();
        }

        return cachedBounds;
    }
}