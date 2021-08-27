package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.vector.Vector3I;

public enum Direction implements Vector3I {
    NORTH(0, 0, -1),
    NORTHEAST(1, 0, -1),
    EAST(1, 0, 0),
    SOUTHEAST(1, 0, 1),
    SOUTH(0, 0, 1),
    SOUTHWEST(-1, 0, 1),
    WEST(-1, 0, 0),
    NORTHWEST(-1, 0, -1),

    UP(0, 1, 0),
    NORTH_UP(0, 1, -1),
    NORTHEAST_UP(1, 1, -1),
    EAST_UP(1, 1, 0),
    SOUTHEAST_UP(1, 1, 1),
    SOUTH_UP(0, 1, 1),
    SOUTHWEST_UP(-1, 1, 1),
    WEST_UP(-1, 1, 0),
    NORTHWEST_UP(-1, 1, -1),

    DOWN(0, -1, 0),
    NORTH_DOWN(0, -1, -1),
    NORTHEAST_DOWN(1, -1, -1),
    EAST_DOWN(1, -1, 0),
    SOUTHEAST_DOWN(1, -1, 1),
    SOUTH_DOWN(0, -1, 1),
    SOUTHWEST_DOWN(-1, -1, 1),
    WEST_DOWN(-1, -1, 0),
    NORTHWEST_DOWN(-1, -1, -1);

    private final int x;
    private final int y;
    private final int z;

    private static final Direction[] values;

    static {
        values = Direction.values();
    }

    Direction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isIntercardinal() {
        return switch (this) {
            case NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST -> true;
            default -> false;
        };
    }

    boolean isCardinal() {
        return switch (this) {
            case NORTH, SOUTH, EAST, WEST -> true;
            default -> false;
        };
    }

    public static Direction valueAtIndex(int position) {
        return values[position];
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }
}
