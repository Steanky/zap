package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.List;

/**
 * Data for a linear gun
 */
@Getter
public class LinearGunData extends GunData<LinearGunLevel> {

    private Particle particle;

    private Object particleData;

    private int maxPierceableEntities;

    private LinearGunData() {

    }

}
