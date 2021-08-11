package io.github.zap.arenaapi.hotbar2;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interactive item which exists in a player's hotbar.
 */
public interface HotbarObject {
    @NotNull PlayerView getOwner();

    @NotNull ItemStack getStack();

    int getSlot();

    @NotNull HotbarObject copyInSlot(int slot);

    void onLeftClick(@NotNull Block interactedWith);

    void onRightClick(@NotNull Block interactedWith);

    boolean isShown();

    void show();

    void hide();

    void activate();

    void deactivate();

    void onSelected();

    void onDeselected();
}
