package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GunData;
import io.github.zap.zombies.game.data.equipment.gun.GunLevel;
import io.github.zap.zombies.game.data.powerups.ModifierModificationPowerUpData;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.util.MathUtils;

/**
 * This power up apply a function f(x) = x * multiplier + amount to all players ammo
 */
@PowerUpType(name = "Ammo-Modification")
// Apply multiplier before amount (addition), allow negative val
public class AmmoModificationPowerUp extends PowerUp {

    public AmmoModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public AmmoModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        getArena().getPlayerMap().forEach((l,r) -> {
            var gunGroup = r.getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            gunGroup.getHotbarObjectMap().forEach((slot, eq) -> {
                if(eq instanceof Gun) {
                    var gun = (Gun<? extends GunData<?>, ? extends GunLevel>)eq;
                    var levelAmmo = gun.getCurrentLevel().getAmmo();
                    var cData = (ModifierModificationPowerUpData) getData();

                    gun.setAmmo((int) MathUtils.normalizeMultiplier(levelAmmo * cData.getMultiplier() + cData.getAmount(), levelAmmo));
                }
            });
        });

        deactivate();
    }
}
