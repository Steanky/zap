package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ChunkIdentifier {
    public final UUID worldID;
    public final ChunkCoordinate coordinate;
    private final int hash;

    public ChunkIdentifier(@NotNull UUID worldID, @NotNull ChunkCoordinate coordinate) {
        this.worldID = worldID;
        this.coordinate = coordinate;
        hash = Objects.hash(worldID, coordinate);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkIdentifier) {
            ChunkIdentifier other = (ChunkIdentifier) obj;
            return other.worldID.equals(worldID) && other.coordinate.equals(coordinate);
        }

        return false;
    }
}
