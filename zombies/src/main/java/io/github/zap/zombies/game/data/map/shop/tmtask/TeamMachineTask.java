package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Represents a task usable by a team machine
 */
@Getter
public abstract class TeamMachineTask {

    private final String type;

    private String displayName;

    private List<String> lore;

    private Material displayMaterial;

    private int initialCost;

    private transient int timesUsed = 0;

    public TeamMachineTask(String type) {
        this.type = type;
    }

    /**
     * Executes the team machine task
     * @param zombiesArena The arena the team machine is in
     * @param zombiesPlayer The executing player
     * @return Whether the execution was successful
     */
    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        int cost = getCost();
        if (zombiesPlayer.getCoins() < cost) {
            zombiesPlayer.getPlayer().sendMessage(ChatColor.RED + "You cannot afford this item!");
        } else {
            timesUsed++;
            zombiesPlayer.subtractCoins(cost);

            return true;
        }

        return false;
    }

    /**
     * Gets the current cost of the team machine task to purchase
     * @return The current cost
     */
    protected abstract int getCost();

}
