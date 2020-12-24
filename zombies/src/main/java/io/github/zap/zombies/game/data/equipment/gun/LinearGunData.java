package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.data.level.GunLevel;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

public class LinearGunData extends GunData {

    @Getter
    private final Particle particle;

    @Getter
    private final Object particleData;

    public LinearGunData(String name, String displayName, List<String> lore, List<GunLevel> levels, Material material, Particle particle, Object particleData) {
        super(name, displayName, lore, levels, material);

        this.particle = particle;
        this.particleData = particleData;
    }
}
