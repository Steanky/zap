package io.github.zap.arenaapi.hotbar2;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interactive item which exists in a player's hotbar.
 */
public interface HotbarObject {
    @NotNull PlayerView owner();

    @NotNull ItemStack getStack();

    int slot();

    void onLeftClick(@NotNull Block interactedWith);

    void onRightClick(@NotNull Block interactedWith);

    void onActivate();

    void onDeactivate();

    void onSelected();

    void onDeselected();
}
