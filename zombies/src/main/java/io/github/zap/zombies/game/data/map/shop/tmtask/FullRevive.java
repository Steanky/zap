package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.ZombiesPlayerState;

/**
 * Task which revives all knocked down players in an arena
 */
public class FullRevive extends TeamMachineTask {

    public FullRevive() {
        super(TeamMachineTaskType.FULL_REVIVE.name());
    }

    @Override
    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(zombiesArena, zombiesPlayer)){
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

    @Override
    public int getCost() {
        return getInitialCost();
    }
}
