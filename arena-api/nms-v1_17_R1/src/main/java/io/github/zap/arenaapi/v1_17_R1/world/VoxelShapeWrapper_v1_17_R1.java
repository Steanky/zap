package io.github.zap.arenaapi.v1_17_R1.world;

import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

class VoxelShapeWrapper_v1_17_R1 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper FULL = new VoxelShapeWrapper_v1_17_R1(Shapes.block());
    public static final VoxelShapeWrapper EMPTY = new VoxelShapeWrapper_v1_17_R1(Shapes.empty());

    private final VoxelShape shape;
    private final List<AABB> bounds;

    VoxelShapeWrapper_v1_17_R1(VoxelShape shape) {
        this.shape = shape;
        bounds = shape.toAabbs();
    }

    @Override
    public double maxY() {
        return shape.max(Direction.Axis.Y);
    }

    @Override
    public double minY() {
        return shape.min(Direction.Axis.Y);
    }

    @Override
    public boolean isFull() {
        return shape == Shapes.block();
    }

    @Override
    public boolean isEmpty() {
        return shape == Shapes.empty();
    }

    @Override
    public @NotNull List<BoundingBox> boundingBoxes() {
        return shape.toAabbs().stream().map(bb -> new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean anyBoundsMatches(@NotNull BoxPredicate predicate) {
        for(AABB bounds : bounds) {
            if(predicate.test(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Shapes.joinIsNotEmpty(shape, Shapes.create(minX, minY, minZ, maxX, maxY, maxZ), BooleanOp.AND);
    }

    public @NotNull VoxelShape getShape() {
        return shape;
    }
}
