package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;
import org.bukkit.util.Vector;

public class Spawnpoint extends DataSerializable {
    @Getter
    private Vector origin;

    @Getter
    private Vector target;

    private Spawnpoint() {}

    public Spawnpoint(Vector origin, Vector target) {
        this.origin = origin;
        this.target = target;
    }
}
