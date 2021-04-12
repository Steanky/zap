package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

class ChunkIdentifier implements ChunkVectorSource {
    public final UUID worldID;
    public final ChunkVectorSource coordinate;
    private final int hash;

    ChunkIdentifier(@NotNull UUID worldID, @NotNull ChunkVectorSource coordinate) {
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

    @Override
    public String toString() {
        return "ChunkIdentifier{worldID=" + worldID + ", coordinate=" + coordinate + "}";
    }

    @Override
    public int chunkX() {
        return coordinate.chunkX();
    }

    @Override
    public int chunkZ() {
        return coordinate.chunkZ();
    }
}
