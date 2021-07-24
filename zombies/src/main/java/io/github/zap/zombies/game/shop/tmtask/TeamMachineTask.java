package io.github.zap.zombies.game.shop.tmtask;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import io.github.zap.zombies.game.data.shop.tmtask.TeamMachineTaskData;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task usable by a team machine
 */
@Getter
public abstract class TeamMachineTask<D extends @NotNull TeamMachineTaskData> {

    private final D teamMachineTaskData;

    private int timesUsed = 0;

    public TeamMachineTask(D teamMachineTaskData) {
        this.teamMachineTaskData = teamMachineTaskData;
    }

    /**
     * Executes the team machine task
     * @param teamMachine The team machine that called the task
     * @param player The executing player
     * @return Whether the execution was successful
     */
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer player) {
        if (player != null) {
            int cost = getCost();
            if (player.getCoins() < cost) {
                player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                        NamedTextColor.RED));

                player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                        Sound.Source.MASTER, 1.0F, 0.5F));
            } else {
                timesUsed++;
                player.subtractCoins(cost);

                return true;
            }
        }

        return false;
    }

    /**
     * Gets the current cost of the team machine task
     */
    protected abstract int getCost();

    /**
     * Gets the item stack representation of the team machine task to be used in a {@link TeamMachine}
     * @return The item stack representation of the team machine task
     */
    public ItemStack getItemStackRepresentationForTeamMachine() {
        ItemStack itemStack = new ItemStack(getTeamMachineTaskData().getDisplayMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(getTeamMachineTaskData().getDisplayName()));

        List<Component> lore = new ArrayList<>();
        for (String line : getTeamMachineTaskData().getLore()) {
            lore.add(Component.text(line));
        }

        lore.add(Component.text("Cost: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%d Gold", getCost()),
                        NamedTextColor.GOLD)));
        itemMeta.lore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
