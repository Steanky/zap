package io.github.zap.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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

    private static final Direction[] VALUES = Direction.values();

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

    public boolean isCardinal() {
        return switch (this) {
            case NORTH, SOUTH, EAST, WEST -> true;
            default -> false;
        };
    }

    @SuppressWarnings("DuplicatedCode") //i can't see the duplicate code here??? unintelliJ moment
    public @NotNull Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTHEAST -> SOUTHWEST;
            case EAST -> WEST;
            case SOUTHEAST -> NORTHWEST;
            case SOUTH -> NORTH;
            case SOUTHWEST -> NORTHEAST;
            case WEST -> EAST;
            case NORTHWEST -> SOUTHEAST;
            case UP -> DOWN;
            case NORTH_UP -> SOUTH_DOWN;
            case NORTHEAST_UP -> SOUTHWEST_DOWN;
            case EAST_UP -> WEST_DOWN;
            case SOUTHEAST_UP -> NORTHWEST_DOWN;
            case SOUTH_UP -> NORTH_DOWN;
            case SOUTHWEST_UP -> NORTHEAST_DOWN;
            case WEST_UP -> EAST_DOWN;
            case NORTHWEST_UP -> SOUTHEAST_DOWN;
            case DOWN -> UP;
            case NORTH_DOWN -> SOUTH_UP;
            case NORTHEAST_DOWN -> SOUTHWEST_UP;
            case EAST_DOWN -> WEST_UP;
            case SOUTHEAST_DOWN -> NORTHWEST_UP;
            case SOUTH_DOWN -> NORTH_UP;
            case SOUTHWEST_DOWN -> NORTHEAST_UP;
            case WEST_DOWN -> EAST_UP;
            case NORTHWEST_DOWN -> SOUTHEAST_UP;
        };
    }

    @SuppressWarnings("DuplicatedCode") //why does unintelliJ say all switch statements are duplicate code
    public @NotNull Direction rotateClockwise() {
        return switch (this) {
            case NORTH -> EAST;
            case NORTHEAST -> SOUTHEAST;
            case EAST -> SOUTH;
            case SOUTHEAST -> SOUTHWEST;
            case SOUTH -> WEST;
            case SOUTHWEST -> NORTHWEST;
            case WEST -> NORTH;
            case NORTHWEST -> NORTHEAST;
            default -> throw new IllegalArgumentException("cannot rotate Direction " + this);
        };
    }

    public static Direction valueAtIndex(int position) {
        return VALUES[position];
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
