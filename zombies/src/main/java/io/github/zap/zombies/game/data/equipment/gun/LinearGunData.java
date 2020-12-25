package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.data.level.GunLevel;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

/**
 * Data for a linear gun
 */
public class LinearGunData extends GunData {

    @Getter
    private Particle particle;

    @Getter
    private Object particleData;

    public LinearGunData(String displayName, List<String> lore, List<GunLevel> levels, Material material, Particle particle, Object particleData) {
        super("linear", displayName, lore, levels, material);

        this.particle = particle;
        this.particleData = particleData;
    }

    private LinearGunData() {

    }
}
