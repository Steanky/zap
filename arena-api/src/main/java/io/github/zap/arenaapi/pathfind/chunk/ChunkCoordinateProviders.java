package io.github.zap.arenaapi.pathfind.chunk;

import io.github.zap.vector.Vector2I;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ChunkCoordinateProviders {
    public static ChunkBounds boundedSquare(@NotNull Vector2I from, @NotNull Vector2I to) {
        Objects.requireNonNull(from, "from cannot be null!");
        Objects.requireNonNull(to, "to cannot be null!");
        return new ChunkBoundsImpl(from, to);
    }

    public static ChunkBounds squareFromCenter(@NotNull Vector2I center, int radius) {
        Objects.requireNonNull(center, "center cannot be null!");
        Validate.isTrue(radius > 0, "radius cannot be negative!");
        return new ChunkBoundsImpl(center, radius);
    }
}
