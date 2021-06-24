package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final ImmutableWorldVector position;

    PathDestinationImpl(@NotNull ImmutableWorldVector position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof PathDestinationImpl) {
            return ((PathDestinationImpl) object).position.equals(position);
        }

        return false;
    }

    @Override
    public @NotNull VectorAccess position() {
        return position;
    }
}
