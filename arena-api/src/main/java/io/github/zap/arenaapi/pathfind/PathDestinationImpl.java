package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.vector2.WorldVector;
import org.jetbrains.annotations.NotNull;

class PathDestinationImpl implements PathDestination {
    private final WorldVector position;

    PathDestinationImpl(@NotNull WorldVector position) {
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
    public @NotNull WorldVector position() {
        return position;
    }
}
