package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.WorldVector;

public enum Direction {
    NORTH(new WorldVector(0, 0, 0)),
    NORTHEAST(new WorldVector(0, 0, 0)),
    EAST(new WorldVector(0, 0, 0)),
    SOUTHEAST(new WorldVector(0, 0, 0)),
    SOUTH(new WorldVector(0, 0, 0)),
    SOUTHWEST(new WorldVector(0, 0, 0)),
    WEST(new WorldVector(0, 0, 0)),
    NORTHWEST(new WorldVector(0, 0, 0));

    private final WorldVector offset;

    public WorldVector offset() {
        return offset;
    }

    Direction(WorldVector offset) {
        this.offset = offset;
    }
}
