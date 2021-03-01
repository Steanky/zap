package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.hotbar.HotbarManager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;

import java.util.Collections;

@PowerUpType(getName = "Max Ammo")
public class MaxAmmoPowerUp extends PowerUp {
    public MaxAmmoPowerUp(PowerUpData data, ZombiesArena arena) {
        super(data, arena);
    }

    @Override
    public void activate() {
        getArena().getPlayerMap().forEach((l,r) -> {
            var gunGroup = r.getHotbarManager().getHotbarObjectGroup(HotbarManager.DEFAULT_PROFILE_NAME);
            gunGroup.getHotbarObjectMap().forEach((slot, eq) -> {
                if(eq instanceof Gun) {
                    ((Gun<?, ?>) eq).refill();
                }
            });
        });

        deactivate();
    }
}
