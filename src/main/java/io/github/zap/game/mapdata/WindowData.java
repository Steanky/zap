package io.github.zap.game.mapdata;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

@AllArgsConstructor
public class WindowData extends DataSerializable {
    @Getter
    private List<Material> materials;

    @Getter
    private List<Vector> vectors;

    @Getter
    private MultiBoundingBox interiorBounds;

    @Getter
    private MultiBoundingBox faceBounds;

    @Getter
    private SpawnpointData spawnpoint;

    private WindowData() {}

    public void breakAtIndex(World world, int index) {
        Vector vector = vectors.get(index);
        world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()).setType(Material.AIR);
    }

    public void repairAtIndex(World world, int index) {
        Material repairMaterial = materials.get(index);
        Vector vector = vectors.get(index);
        world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()).setType(repairMaterial);
    }
}
