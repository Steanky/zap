package io.github.zap.nms;

import io.github.zap.nms.world.WrappedVoxelShape;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.VoxelShape;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class WrappedVoxelShape_v1_16_R3 extends Wrapper<VoxelShape> implements WrappedVoxelShape {
    private final List<AxisAlignedBB> boundingBoxes;

    WrappedVoxelShape_v1_16_R3(@NotNull VoxelShape handle) {
        super(handle);
        boundingBoxes = handle.d();
    }

    @Override
    public boolean containsPoint(double x, double y, double z) {
        for(AxisAlignedBB box : boundingBoxes) {
            if(box.e(x, y, z)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overlaps(@NotNull BoundingBox boundingBox) {
        for(AxisAlignedBB box : boundingBoxes) {
            if(box.intersects(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                    boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ())) {
                return true;
            }
        }

        return false;
    }
}