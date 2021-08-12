package io.github.zap.arenaapi.hotbar2;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interactive item which exists in a player's hotbar.
 */
public interface HotbarObject {
    @NotNull HotbarCanvas getCanvas();

    @NotNull ItemStack getStack();

    int getSlot();

    @NotNull HotbarObject copyInSlot(int slot);

    void onPlayerInteract(@NotNull PlayerInteractEvent event);

    boolean isShown();

    void show();

    void hide();

    void cleanup();

    void refresh();

    boolean isSelected();

    void onSelected();

    void onDeselected();
}
