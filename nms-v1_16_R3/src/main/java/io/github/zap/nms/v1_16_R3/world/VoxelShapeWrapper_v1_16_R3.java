package io.github.zap.nms.v1_16_R3.world;

import io.github.zap.nms.common.world.VoxelShapeWrapper;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.VoxelShape;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    private final List<AxisAlignedBB> boundingBoxes;

    VoxelShapeWrapper_v1_16_R3(@NotNull VoxelShape handle) {
        boundingBoxes = handle.d();
    }

    VoxelShapeWrapper_v1_16_R3(@NotNull List<AxisAlignedBB> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    @Override
    public boolean containsPoint(double relativeX, double relativeY, double relativeZ) {
        for(AxisAlignedBB box : boundingBoxes) {
            if(box.e(relativeX, relativeY, relativeZ)) {
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