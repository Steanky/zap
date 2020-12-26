package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.data.level.GunLevel;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

/**
 * Data for a linear gun
 */
@Getter
public class LinearGunData extends GunData {

    private Particle particle;

    private Object particleData;

    public LinearGunData(String name, String displayName, List<String> lore, List<GunLevel> levels, Material material, Particle particle, Object particleData) {
        super(name, displayName, lore, levels, material, "linear");

        this.particle = particle;
        this.particleData = particleData;
    }

    private LinearGunData() {

    }

}
