package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VectorAgent implements PathAgent {
    private final Vector vector;
    private final Characteristics characteristics;

    VectorAgent(@NotNull Vector vector, @NotNull Characteristics characteristics) {
        this.vector = vector.clone();
        this.characteristics = characteristics;
    }

    @Override
    public @NotNull Characteristics characteristics() {
        return characteristics;
    }

    @Override
    public @NotNull Vector position() {
        return vector.clone();
    }
}
