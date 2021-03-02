package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunData;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunLevel;

public class GuardianGun extends Gun<GuardianGunData, GuardianGunLevel> {

    public GuardianGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, GuardianGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {

    }
}
