package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class WindowData extends DataSerializable {
    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private Vector[] components;

    @Getter
    private Material[] materials; //TODO: converter for material arrays

    @Getter
    private Spawnpoint spawnpoint;

    private WindowData() {}

    public WindowData(MultiBoundingBox bounds, Vector[] components, Material[] materials, Spawnpoint spawnpoint) {
        this.bounds = bounds;
        this.components = components;
        this.materials = materials;
        this.spawnpoint = spawnpoint;
    }
}
