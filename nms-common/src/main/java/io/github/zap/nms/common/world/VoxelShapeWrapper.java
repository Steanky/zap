package io.github.zap.nms.common.world;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface VoxelShapeWrapper {
    VoxelShapeWrapper FULL_BLOCK = new VoxelShapeWrapper() {
        @Override
        public void forEachBox(@NotNull BoxConsumer consumer) {
            consumer.consume(0, 0, 0, 1, 1, 1);
        }

        @Override
        public double maxY() {
            return 1;
        }

        @Override
        public double minY() {
            return 0;
        }

        @Override
        public boolean isFull() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public List<BoundingBox> boundingBoxes() {
            return new ArrayList<>();
        }
    };

    VoxelShapeWrapper EMPTY_BLOCK = new VoxelShapeWrapper() {
        @Override
        public void forEachBox(@NotNull BoxConsumer consumer) { }

        @Override
        public double maxY() {
            return 0;
        }

        @Override
        public double minY() {
            return 0;
        }

        @Override
        public boolean isFull() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public List<BoundingBox> boundingBoxes() {
            return new ArrayList<>();
        }
    };

    void forEachBox(@NotNull BoxConsumer consumer);

    double maxY();

    double minY();

    boolean isFull();

    boolean isEmpty();

    List<BoundingBox> boundingBoxes();
}
