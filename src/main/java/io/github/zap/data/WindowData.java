package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@AllArgsConstructor
public class WindowData extends DataSerializable {
    private MultiBoundingBox bounds;
    private List<Vector> components;
    private List<Material> materials; //TODO: converter for material arrays
    private SpawnpointData spawnpoint;

    private WindowData() {}
}
