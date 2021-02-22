package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunData;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import org.bukkit.Location;

import java.util.Random;

/**
 * Represents a gun that shoots a spray of bullets, similarly to linear guns
 */
public class SprayGun extends Gun<SprayGunData, SprayGunLevel> {

    private static final Random RANDOM = new Random();

    public SprayGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, SprayGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {
        Location eyeLocation = getPlayer().getEyeLocation();

        SprayGunData sprayGunData = getEquipmentData();
        SprayGunLevel currentLevel = sprayGunData.getLevels().get(getLevel());

        for (int i = 0; i < currentLevel.getPellets(); i++) {
            float angle = currentLevel.getConeAngle();
            float dYaw = angle * (2 * RANDOM.nextFloat() - 1F), dPitch = angle * (2 * RANDOM.nextFloat() - 1F);

            Location eyeLocationCopy = eyeLocation.clone();
            eyeLocationCopy.setYaw(eyeLocationCopy.getYaw() + dYaw);
            eyeLocationCopy.setPitch(eyeLocationCopy.getPitch() + dPitch);

            new LinearBeam(
                    getZombiesArena().getMap(),
                    eyeLocationCopy,
                    sprayGunData.getParticle(),
                    currentLevel
            ).send();
        }
    }

}
