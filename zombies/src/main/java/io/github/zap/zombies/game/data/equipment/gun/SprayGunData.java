package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

/**
 * Data for a spray gun
 */
@Getter
public class SprayGunData extends GunData<SprayGunLevel> {

    private Particle particle;

    private Object particleData;

    public SprayGunData(String name, String displayName, List<String> lore, List<SprayGunLevel> levels,
                        Material material) {
        super(EquipmentType.SPRAY_GUN.name(), name, displayName, lore, levels, material);
    }

}
