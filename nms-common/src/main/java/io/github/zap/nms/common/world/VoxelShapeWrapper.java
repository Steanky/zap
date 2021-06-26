package io.github.zap.nms.common.world;

import org.jetbrains.annotations.NotNull;

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
    };

    VoxelShapeWrapper EMPTY_BLOCK = new VoxelShapeWrapper() {
        @Override
        public void forEachBox(@NotNull BoxConsumer consumer) {
            consumer.consume(0, 0, 0, 0, 0, 0);
        }

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
    };

    void forEachBox(@NotNull BoxConsumer consumer);

    double maxY();

    double minY();

    boolean isFull();

    boolean isEmpty();
}
