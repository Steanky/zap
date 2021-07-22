package io.github.zap.arenaapi.hotbar;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single object in a hotbar
 */
public class HotbarObject {

    private final @NotNull OfflinePlayer player;

    private final int slot;

    private ItemStack representingItemStack = null;

    private boolean visible = false;

    private boolean selected = false;

    private boolean removed = false;

    /**
     * Creates a hotbar object that is invisible
     * @param player The player the hotbar object belongs to
     * @param slot The slot of the hotbar object
     */
    public HotbarObject(@NotNull OfflinePlayer player, int slot) {
        this.player = player;
        this.slot = slot;
    }

    /**
     * Creates a hotbar object that is invisible that would display an item stack
     * @param player The player the hotbar object belongs to
     * @param slot The slot of the hotbar object
     * @param representingItemStack The item stack to display
     */
    public HotbarObject(@NotNull OfflinePlayer player, int slot, @Nullable ItemStack representingItemStack) {
        this(player, slot);
        this.representingItemStack = representingItemStack;
    }

    /**
     * Sets the item stack in the hotbar object slot
     * @param itemStack The item stack to display
     */
    private void setStack(@Nullable ItemStack itemStack) {
        if (!removed && visible) {
            Player onlinePlayer = tryGetPlayer();
            onlinePlayer.getInventory().setItem(slot, itemStack);
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
    public void setRepresentingItemStack(@Nullable ItemStack representingItemStack) {
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

            Player onlinePlayer = tryGetPlayer();
            if (onlinePlayer.getInventory().getHeldItemSlot() == slot) {
                onSlotSelected();
            }
        } else {
            onSlotDeselected();
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

    /**
     * Gets the offline player that this hotbar object is held by
     * @return The hotbar object slot
     */
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Tries to get the online player
     * If the player is not online, throws {@link IllegalStateException}
     * @return The online player
     */
    public @NotNull Player tryGetPlayer() {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            throw new IllegalStateException("Player " + player.getName() + " is not online!");
        }

        return onlinePlayer;
    }

    /**
     * Gets the slot this hotbar object should be in
     * @return The slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the representing item stack of this hotbar object
     * @return The item stack
     */
    public @Nullable ItemStack getRepresentingItemStack() {
        return representingItemStack;
    }

    /**
     * Whether this hotbar object is visible
     * @return Visibility state
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Whether this hotbar object is selected
     * @return Selection state
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Whether this hotbar object has been removed
     * @return Removal state
     */
    public boolean isRemoved() {
        return removed;
    }

}
