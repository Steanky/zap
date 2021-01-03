package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import org.bukkit.entity.Player;

/**
 * Represents a gun which shoots a line of particles and damages guns within a line
 */
public class LinearGun extends Gun<LinearGunData, LinearGunLevel> {

    public LinearGun(Player player, int slot, LinearGunData equipmentData) {
        super(player, slot, equipmentData);
    }

    @Override
    public void shoot() {
        LinearGunData linearGunData = getEquipmentData();
        LinearGunLevel currentLevel = linearGunData.getLevels().get(getLevel());

        new LinearBeam(
                getPlayer().getEyeLocation(),
                linearGunData.getParticle(),
                currentLevel.getMaxPierceableEntities(),
                currentLevel.getDamage(),
                currentLevel.getRange()
        );
    }
}
