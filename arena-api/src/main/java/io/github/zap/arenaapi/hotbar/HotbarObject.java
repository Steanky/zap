package io.github.zap.arenaapi.hotbar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single object in a hotbar
 */
@RequiredArgsConstructor
@Getter
public class HotbarObject {

    private final Player player;

    private final int slot;

    private ItemStack representingItemStack = null;

    private boolean visible = false;

    private boolean selected = false;

    private boolean removed = false;

    /**
     * Creates a hotbar object that is invisible that would display an item stack
     * @param player The player the hotbar object belongs to
     * @param slot The slot of the hotbar object
     * @param representingItemStack The item stack to display
     */
    public HotbarObject(@NotNull Player player, int slot, @NotNull ItemStack representingItemStack) {
        this(player, slot);
        this.representingItemStack = representingItemStack;
    }

    /**
     * Sets the item stack in the hotbar object slot
     * @param itemStack The item stack to display
     */
    private void setStack(@Nullable ItemStack itemStack) {
        if (!removed && visible) {
            player.getInventory().setItem(slot, itemStack);
        }
    }

    /**
     * Method to call when the slot is selected in the hotbar
     */
    public void onSlotSelected() {
        selected = true;
    }

    /**
     * Method to call when the slot is unselected in the hotbar
     */
    public void onSlotDeselected() {
        selected = false;
    }

    /**
     * Method to call when the slot is left clicked in the hotbar
     * @param action The action that triggered the left click
     */
    public void onLeftClick(@NotNull Action action) {

    }

    /**
     * Method to call when the slot is right clicked in the hotbar
     * @param action The action that triggered the right click
     */
    public void onRightClick(@NotNull Action action) {

    }

    /**
     * Sets the representing item stack of the hotbar
     * @param representingItemStack The item stack to represent
     */
    public void setRepresentingItemStack(@NotNull ItemStack representingItemStack) {
        this.representingItemStack = representingItemStack;

        if (visible) {
            setStack(representingItemStack);
        }
    }

    /**
     * Sets the visibility of the hotbar object
     * @param visible Whether or not the hotbar object is visible
     */
    public void setVisible(boolean visible) {
        if (this.visible = visible) {
            setStack(representingItemStack);
            if (player.getInventory().getHeldItemSlot() == slot) {
                onSlotSelected();
            }
        }
    }

    /**
     * Removes the hotbar object
     */
    public void remove() {
        if (visible) {
            setStack(null);
        }
        removed = true;
    }

}
