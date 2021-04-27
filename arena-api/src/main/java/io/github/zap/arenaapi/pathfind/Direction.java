package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ImmutableWorldVector;
import io.github.zap.arenaapi.vector.VectorAccess;

public enum Direction {
    NORTH(VectorAccess.immutable(0D, 0D, -1D)),
    NORTHEAST(VectorAccess.immutable(1D, 0D, -1D)),
    EAST(VectorAccess.immutable(1D, 0D, 0D)),
    SOUTHEAST(VectorAccess.immutable(1D, 0D, 1D)),
    SOUTH(VectorAccess.immutable(0D, 0D, 1D)),
    SOUTHWEST(VectorAccess.immutable(-1D, 0D, 1D)),
    WEST(VectorAccess.immutable(-1D, 0D, 0D)),
    NORTHWEST(VectorAccess.immutable(-1D, 0D, -1D)),
    UP(VectorAccess.immutable(0D, 1D, 0D)),
    DOWN(VectorAccess.immutable(0D, -1D, 0D));

    private final VectorAccess offset;

    Direction(VectorAccess offset) {
        this.offset = offset;
    }

    public VectorAccess offset() {
        return offset;
    }
}
