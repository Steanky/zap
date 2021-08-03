package io.github.zap.arenaapi.nms.common.world;

@FunctionalInterface
public interface BoxPredicate {
    boolean test(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
