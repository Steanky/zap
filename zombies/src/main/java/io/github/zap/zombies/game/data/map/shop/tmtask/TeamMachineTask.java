package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.Material;

@Getter
public abstract class TeamMachineTask {

    private String type;

    private Material displayMaterial;

    private int initialCost;

    private transient int timesUsed = 0;

    public TeamMachineTask(String type) {
        this.type = type;
    }

    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        int cost = getCost();
        if (zombiesPlayer.getCoins() < cost) {
            // TODO: poor
        } else {
            timesUsed++;
            zombiesPlayer.subtractCoins(cost);

            return true;
        }

        return false;
    }

    public abstract int getCost();

}
