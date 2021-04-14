package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector.ChunkVectorSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

class ChunkIdentifier {
    public final UUID worldID;
    public final ChunkVectorSource position;
    private final int hash;

    ChunkIdentifier(@NotNull UUID worldID, @NotNull ChunkVectorSource position) {
        this.worldID = worldID;
        this.position = position;
        hash = Objects.hash(worldID, position);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkIdentifier) {
            ChunkIdentifier other = (ChunkIdentifier) obj;
            return other.worldID.equals(worldID) && other.position.equals(position);
        }

        return false;
    }

    @Override
    public String toString() {
        return "ChunkIdentifier{worldID=" + worldID + ", position=" + position + "}";
    }

    public ChunkVectorSource position() {
        return position;
    }
}