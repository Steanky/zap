package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interactive item which exists in a player's hotbar.
 */
public interface HotbarObject {
    @NotNull ItemStack getStack();

    @NotNull HotbarObject copyInSlot(int slot);

    void onPlayerInteract(@NotNull PlayerInteractEvent event);

    void cleanup();

    void onSelected();

    void onDeselected();
}
