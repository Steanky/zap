package io.github.zap.zombies.game.shop.tmtask;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.equipment.gun.GunObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import io.github.zap.zombies.game.data.shop.tmtask.AmmoSupplyData;
import org.jetbrains.annotations.NotNull;

/**
 * Task which refills all gun ammo in a player team
 */
public class AmmoSupply extends TeamMachineTask<@NotNull AmmoSupplyData> {

    public AmmoSupply(@NotNull AmmoSupplyData ammoSupplyData) {
        super(ammoSupplyData);
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer player) {
        if (super.execute(teamMachine, zombiesArena, player)) {
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                GunObjectGroup gunObjectGroup = (GunObjectGroup)
                        otherZombiesPlayer.getHotbarManager().getHotbarObjectGroup(EquipmentObjectGroupType.GUN.name());

                if (gunObjectGroup != null) {
                    for (HotbarObject hotbarObject : gunObjectGroup.getHotbarObjectMap().values()) {
                        if (hotbarObject instanceof Gun<?, ?> gun) {
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
        return getTeamMachineTaskData().getInitialCost();
    }

}
