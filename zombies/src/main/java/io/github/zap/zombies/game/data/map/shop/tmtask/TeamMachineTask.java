package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.Material;

/**
 * Represents a task usable by a team machine
 */
@Getter
public abstract class TeamMachineTask {

    private final transient LocalizationManager localizationManager;

    private final String type;

    private String displayName;

    private Material displayMaterial;

    private int initialCost;

    private transient int timesUsed = 0;

    public TeamMachineTask(String type) {
        this.localizationManager = Zombies.getInstance().getLocalizationManager();
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
            localizationManager.sendLocalizedMessage(zombiesPlayer.getPlayer(), MessageKey.CANNOT_AFFORD.getKey());
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
