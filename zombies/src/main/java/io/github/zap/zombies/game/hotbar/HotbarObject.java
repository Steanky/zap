package io.github.zap.zombies.game.hotbar;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A single object in a hotbar
 */
@Getter
public class HotbarObject {

    private final Player player;

    private final int slotId;

    private ItemStack representingItemStack = null;

    private boolean visible = false;

    private boolean selected = false;

    private boolean removed = false;

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
     * Creates a hotbar object that is invisible that would display an item stack
     * @param player The player the hotbar object belongs to
     * @param slotId The slot of the hotbar object
     * @param representingItemStack The item stack to display
     */
    public HotbarObject(Player player, int slotId, ItemStack representingItemStack) {
        this(player, slotId);
        this.representingItemStack = representingItemStack;
    }

    /**
     * Sets the item stack in the hotbar object slot
     * @param itemStack The item stack to display
     */
    private void setStack(ItemStack itemStack) {
        if (!removed && visible) {
            player.getInventory().setItem(slotId, itemStack);
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
     */
    public void onLeftClick() {

    }

    /**
     * Method to call when the slot is right clicked in the hotbar
     */
    public void onRightClick() {

    }

    /**
     * Sets the representing item stack of the hotbar
     * @param representingItemStack The item stack to represent
     */
    public void setRepresentingItemStack(ItemStack representingItemStack) {
        this.representingItemStack = representingItemStack;

        if (visible) {
            setVisible(true);
        }
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
