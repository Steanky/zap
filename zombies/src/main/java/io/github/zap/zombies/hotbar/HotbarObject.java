package io.github.zap.zombies.hotbar;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A single object in a hotbar
 */
public class HotbarObject {

    protected final Player player;

    @Setter
    protected int slotId;

    @Setter
    @Getter
    protected ItemStack representingItemStack = null;

    protected boolean visible = false;

    protected boolean removed = false;

    /**
     * Creates a hotbar object that is invisible
     * @param player The player the hotbar object belongs to
     * @param slotId The slot of the hotbar object
     */
    public HotbarObject(Player player, int slotId) {
        this.player = player;
        this.slotId = slotId;
    }

    /**
     * Creates a hotbar object that is invisible that would display an itemstack
     * @param player The player the hotbar object belongs to
     * @param slotId The slot of the hotbar object
     * @param representingItemStack The itemstack to display
     */
    public HotbarObject(Player player, int slotId, ItemStack representingItemStack) {
        this(player, slotId);
        this.representingItemStack = representingItemStack;
    }

    /**
     * Sets the item stack in the hotbar object slot
     * @param itemStack The item stack to display
     */
    public void setStack(ItemStack itemStack) {
        if (!removed) {
            player.getInventory().setItem(slotId, itemStack);
        }
    }

    /**
     * Sets the item stack in the hotbar object slot
     * @return The item stack, or null if the hotbar object is not visible or removed
     */
    public ItemStack getStack() {
        if (!removed && visible) {
            return player.getInventory().getItem(slotId);
        } else {
            return null;
        }
    }

    /**
     * Method to call when the slot is selected in the hotbar
     */
    public void onSlotSelected() {

    }

    /**
     * Method to call when the slot is unselected in the hotbar
     */
    public void onSlotDeselected() {

    }

    /**
     * Sets the visibility of the hotbar object
     * @param visible Whether or not the hotbar object is visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        setStack(representingItemStack);
        if (visible && player.getInventory().getHeldItemSlot() == slotId) {
            this.onSlotSelected();
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
