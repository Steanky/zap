package io.github.zap.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface VoxelShapeWrapper {
    VoxelShapeWrapper FULL_BLOCK = new VoxelShapeWrapper() {
        private final BoundingBox bounds = BoundingBox.of(new Vector(0, 0, 0), new Vector(1, 1, 1));

        @Override
        public boolean containsPoint(double relativeX, double relativeY, double relativeZ) {
            return bounds.contains(relativeX, relativeY, relativeZ);
        }

        @Override
        public boolean overlaps(@NotNull BoundingBox boundingBox) {
            return bounds.overlaps(boundingBox);
        }
    };

    VoxelShapeWrapper EMPTY = new VoxelShapeWrapper() {
        @Override
        public boolean containsPoint(double relativeX, double relativeY, double relativeZ) {
            return false;
        }

        @Override
        public boolean overlaps(@NotNull BoundingBox boundingBox) {
            return false;
        }
    };

    boolean containsPoint(double relativeX, double relativeY, double relativeZ);

    boolean overlaps(@NotNull BoundingBox boundingBox);
}
