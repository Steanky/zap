package io.github.zap.zombies.game.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayerState;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import io.github.zap.zombies.game.data.shop.tmtask.FullReviveData;
import org.jetbrains.annotations.NotNull;

/**
 * Task which revives all knocked down players in an arena
 */
public class FullRevive extends TeamMachineTask<@NotNull FullReviveData> {

    public FullRevive(@NotNull FullReviveData fullReviveData) {
        super(fullReviveData);
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer player) {
        if (super.execute(teamMachine, zombiesArena, player)){
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                if (otherZombiesPlayer.isInGame()) {
                    if (otherZombiesPlayer.getState() == ZombiesPlayerState.KNOCKED) {
                        otherZombiesPlayer.revive();
                    } else if (otherZombiesPlayer.getState() == ZombiesPlayerState.DEAD) {
                        otherZombiesPlayer.respawn();
                    }
                }
            }

            return true;
        }

        return false;
    }

    public int getCost() {
        return getTeamMachineTaskData().getInitialCost();
    }
}
