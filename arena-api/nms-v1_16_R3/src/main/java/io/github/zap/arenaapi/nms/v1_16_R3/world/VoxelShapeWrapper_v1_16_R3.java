package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.vector.Bounds;
import io.github.zap.vector.Direction;
import io.github.zap.vector.Vector3D;
import io.github.zap.vector.Vectors;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.scheduler.CraftScheduler;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper FULL = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.fullCube());
    public static final VoxelShapeWrapper EMPTY = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.empty());

    private final VoxelShape shape;
    private Bounds[] bounds = null;

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
    }

    private Bounds[] getBounds() {
        if(bounds == null) {
            List<AxisAlignedBB> aabbs = shape.d();
            bounds = new Bounds[aabbs.size()];

            int i = 0;
            for(AxisAlignedBB bb : aabbs) {
                bounds[i++] = new Bounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY,bb.maxZ);
            }
        }

        return bounds;
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
    public boolean isPartial() {
        return shape != VoxelShapes.empty() && shape != VoxelShapes.fullCube();
    }

    @Override
    public int size() {
        return getBounds().length;
    }

    @Override
    public @NotNull Bounds boundsAt(int index) {
        return getBounds()[index];
    }

    @Override
    public boolean anyBoundsMatches(@NotNull BoxPredicate predicate) {
        for(Bounds bounds : getBounds()) {
            if(predicate.test(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for(Bounds bound : getBounds()) {
            if(bound.overlaps(minX, minY, minZ, maxX, maxY, maxZ)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull Vector3D positionAtSide(@NotNull Direction direction) {
        return switch (direction) {
            case NORTH -> Vectors.of(0, 0, shape.b(EnumDirection.EnumAxis.Z));
            case NORTHEAST -> Vectors.of(shape.c(EnumDirection.EnumAxis.X), 0, shape.b(EnumDirection.EnumAxis.Z));
            case EAST -> Vectors.of(shape.c(EnumDirection.EnumAxis.X), 0, 0);
            case SOUTHEAST -> Vectors.of(shape.c(EnumDirection.EnumAxis.X), 0, shape.c(EnumDirection.EnumAxis.Z));
            case SOUTH -> Vectors.of(0, 0, shape.c(EnumDirection.EnumAxis.Z));
            case SOUTHWEST -> Vectors.of(shape.b(EnumDirection.EnumAxis.X), 0, shape.c(EnumDirection.EnumAxis.Z));
            case WEST -> Vectors.of(shape.b(EnumDirection.EnumAxis.X), 0, 0);
            case NORTHWEST -> Vectors.of(shape.b(EnumDirection.EnumAxis.X), 0, shape.b(EnumDirection.EnumAxis.Z));
            case UP -> Vectors.of(0, shape.c(EnumDirection.EnumAxis.Y), 0);
            case DOWN -> Vectors.of(0, shape.b(EnumDirection.EnumAxis.Y), 0);
            default -> throw new IllegalArgumentException("Direction " + direction + " not a valid side");
        };
    }

    public @NotNull VoxelShape getShape() {
        return shape;
    }
}
