package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final ImmutableWorldVector position;
    private final PathTarget target;

    PathDestinationImpl(@NotNull ImmutableWorldVector position, @NotNull PathTarget target) {
        this.position = VectorAccess.immutable(position.blockX(), position.blockY(), position.blockZ());
        this.target = target;
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

    @Override
    public @NotNull PathTarget target() {
        return target;
    }
}
