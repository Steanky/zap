package io.github.zap.arenaapi.nms.common.world;

@FunctionalInterface
public interface BoxConsumer {
    void consume(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
