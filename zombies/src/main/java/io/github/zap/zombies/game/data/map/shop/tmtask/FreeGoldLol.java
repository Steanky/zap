package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;

public class FreeGoldLol extends TeamMachineTask {

    private int gold;

    public FreeGoldLol(String type) {
        super(type);
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer)) {
            for (ZombiesPlayer otherZombiesPlayer : zombiesArena.getPlayerMap().values()) {
                otherZombiesPlayer.addCoins(gold);
            }

            return true;
        }

        return false;
    }

    @Override
    protected int getCostForTeamMachine(TeamMachine teamMachine) {
        return 0;
    }

}
