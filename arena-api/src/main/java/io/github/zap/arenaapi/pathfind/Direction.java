package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector2.BlockVector;
import io.github.zap.arenaapi.vector2.WorldVector;

public enum Direction {
    NORTH(WorldVector.immutable(0D, 0D, -1D)),
    NORTHEAST(WorldVector.immutable(1D, 0D, -1D)),
    EAST(WorldVector.immutable(1D, 0D, 0D)),
    SOUTHEAST(WorldVector.immutable(1D, 0D, 1D)),
    SOUTH(WorldVector.immutable(0D, 0D, 1D)),
    SOUTHWEST(WorldVector.immutable(-1D, 0D, 1D)),
    WEST(WorldVector.immutable(-1D, 0D, 0D)),
    NORTHWEST(WorldVector.immutable(-1D, 0D, -1D));

    private final WorldVector offset;

    Direction(WorldVector offset) {
        this.offset = offset;
    }

    public WorldVector offset() {
        return offset;
    }
}
