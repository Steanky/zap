package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;

public class AmmoSupply extends TeamMachineTask {

    public AmmoSupply() {
        super(TeamMachineTaskType.AMMO_SUPPLY.name());
    }

    @Override
    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(zombiesArena, zombiesPlayer)) {
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                GunObjectGroup gunObjectGroup = (GunObjectGroup)
                        otherZombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentType.GUN.name());
                if (gunObjectGroup != null) {
                    for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?>) {
                            Gun<?, ?> gun = (Gun<?, ?>) hotbarObject;
                            gun.refill();
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int getCost() {
        return getInitialCost();
    }
}
