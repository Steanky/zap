package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector2I;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

class ChunkIdentifier {
    public final UUID worldID;
    public final Vector2I position;

    ChunkIdentifier(@NotNull UUID worldID, @NotNull Vector2I position) {
        this.worldID = worldID;
        this.position = position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldID, position);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkIdentifier other) {
            return other.worldID.equals(worldID) && other.position.equals(position);
        }

        return false;
    }

    @Override
    public String toString() {
        return "ChunkIdentifier{worldID=" + worldID + ", position=" + position + "}";
    }

    public Vector2I position() {
        return position;
    }
}
