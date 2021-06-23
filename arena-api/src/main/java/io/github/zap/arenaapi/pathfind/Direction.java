package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.Positional;
import io.github.zap.arenaapi.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

public enum Direction implements Positional {
    NORTH(VectorAccess.immutable(0D, 0D, -1D)),
    NORTHEAST(VectorAccess.immutable(1D, 0D, -1D)),
    EAST(VectorAccess.immutable(1D, 0D, 0D)),
    SOUTHEAST(VectorAccess.immutable(1D, 0D, 1D)),
    SOUTH(VectorAccess.immutable(0D, 0D, 1D)),
    SOUTHWEST(VectorAccess.immutable(-1D, 0D, 1D)),
    WEST(VectorAccess.immutable(-1D, 0D, 0D)),
    NORTHWEST(VectorAccess.immutable(-1D, 0D, -1D)),

    UP(VectorAccess.immutable(0D, 1D, 0D)),
    NORTH_UP(VectorAccess.immutable(0D, 1D, -1D)),
    NORTHEAST_UP(VectorAccess.immutable(1D, 1D, -1D)),
    EAST_UP(VectorAccess.immutable(1D, 1D, 0D)),
    SOUTHEAST_UP(VectorAccess.immutable(1D, 1D, 1D)),
    SOUTH_UP(VectorAccess.immutable(0D, 1D, 1D)),
    SOUTHWEST_UP(VectorAccess.immutable(-1D, 1D, 1D)),
    WEST_UP(VectorAccess.immutable(-1D, 1D, 0D)),
    NORTHWEST_UP(VectorAccess.immutable(-1D, 1D, -1D)),

    DOWN(VectorAccess.immutable(0D, -1D, 0D)),
    NORTH_DOWN(VectorAccess.immutable(0D, -1D, -1D)),
    NORTHEAST_DOWN(VectorAccess.immutable(1D, -1D, -1D)),
    EAST_DOWN(VectorAccess.immutable(1D, -1D, 0D)),
    SOUTHEAST_DOWN(VectorAccess.immutable(1D, -1D, 1D)),
    SOUTH_DOWN(VectorAccess.immutable(0D, -1D, 1D)),
    SOUTHWEST_DOWN(VectorAccess.immutable(-1D, -1D, 1D)),
    WEST_DOWN(VectorAccess.immutable(-1D, -1D, 0D)),
    NORTHWEST_DOWN(VectorAccess.immutable(-1D, -1D, -1D));

    private final VectorAccess offset;

    Direction(VectorAccess offset) {
        this.offset = offset;
    }

    @Override
    public @NotNull VectorAccess position() {
        return offset;
    }
}
