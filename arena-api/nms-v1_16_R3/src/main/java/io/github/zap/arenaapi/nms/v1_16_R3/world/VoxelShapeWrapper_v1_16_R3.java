package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.Vectors;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.scheduler.CraftScheduler;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper FULL = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.fullCube());
    public static final VoxelShapeWrapper EMPTY = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.empty());

    private final VoxelShape shape;
    private final List<AxisAlignedBB> bounds;

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
        bounds = shape.d();
    }

    @Override
    public double maxX() {
        return shape.c(EnumDirection.EnumAxis.X);
    }

    @Override
    public double minX() {
        return shape.b(EnumDirection.EnumAxis.X);
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
    public double maxZ() {
        return shape.c(EnumDirection.EnumAxis.Z);
    }

    @Override
    public double minZ() {
        return shape.b(EnumDirection.EnumAxis.Z);
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
        return bounds.stream().map(bb -> new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean anyBoundsMatches(@NotNull BoxPredicate predicate) {
        for(AxisAlignedBB bounds : bounds) {
            if(predicate.test(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for(AxisAlignedBB bound : bounds) {
            if(bound.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                return true;
            }
        }

        return false;
    }

    public @NotNull VoxelShape getShape() {
        return shape;
    }
}
