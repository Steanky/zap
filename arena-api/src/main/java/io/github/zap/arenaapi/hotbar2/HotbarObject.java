package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an interactive item which exists in a player's hotbar.
 */
public interface HotbarObject {
    class Slotted {
        private final HotbarObject hotbarObject;
        private final int slot;

        Slotted(@Nullable HotbarObject object, int slot) {
            this.hotbarObject = object;
            this.slot = slot;
        }

        public @Nullable HotbarObject getHotbarObject() {
            return hotbarObject;
        }

        public int getSlot() {
            return slot;
        }
    }

    @Nullable ItemStack getStack();

    void onPlayerInteract(@NotNull PlayerInteractEvent event);

    void cleanup();

    boolean isSelected();

    void setSelected(@NotNull PlayerItemHeldEvent event);

    void setDeselected(@NotNull PlayerItemHeldEvent event);
}
