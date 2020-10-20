package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class WindowData extends DataSerializable {
    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private Vector[] components;

    @Getter
    private Material[] materials;

    @Getter
    private Vector spawnpoint;

    @Getter
    private Vector target;

    private WindowData() {}

    public WindowData(MultiBoundingBox bounds, Vector[] components, Material[] materials, Vector spawnpoint,
                      Vector target) {
        this.bounds = bounds;
        this.components = components;
        this.materials = materials;
        this.spawnpoint = spawnpoint;
        this.target = target;
    }
}
