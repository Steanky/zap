package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;

public class FullRevive extends TeamMachineTask {

    public FullRevive() {
        super(TeamMachineTaskType.FULL_REVIVE.name());
    }

    @Override
    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(zombiesArena, zombiesPlayer)){
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                if (otherZombiesPlayer.isInGame() && !otherZombiesPlayer.isAlive()) {
                    otherZombiesPlayer.revive();
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
