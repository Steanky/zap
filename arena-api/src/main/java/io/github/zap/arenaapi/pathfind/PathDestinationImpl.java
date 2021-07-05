package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final ImmutableWorldVector position;

    PathDestinationImpl(@NotNull ImmutableWorldVector position) {
        this.position = VectorAccess.immutable(position.blockX(), position.blockY(), position.blockZ());
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof PathDestinationImpl pathDestination) {
            return pathDestination.position.equals(position);
        }

        return false;
    }

    @Override
    public @NotNull VectorAccess position() {
        return position;
    }
}
