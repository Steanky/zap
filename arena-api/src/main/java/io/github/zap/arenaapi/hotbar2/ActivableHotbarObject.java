package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link HotbarObject} which supports basic activation/deactivation, a static owner, and a static slot.
 * Most HotbarObject implementations will want to extend this class, as it will set the appropriate ItemStack upon
 * activation/deactivation.
 */
public abstract class ActivableHotbarObject implements HotbarObject {
    protected final PlayerView owner;
    protected final ItemStack inactiveStack;
    protected final int slot;

    protected ActivableHotbarObject(@NotNull PlayerView owner, @NotNull ItemStack inactiveStack, int slot) {
        this.owner = owner;
        this.inactiveStack = inactiveStack;
        this.slot = slot;
    }

    @Override
    public @NotNull PlayerView getOwner() {
        return owner;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onActivate() {
        owner.getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(getSlot(), getStack()));
    }

    @Override
    public void onDeactivate() {
        owner.getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(getSlot(), getInactiveStack()));
    }

    public abstract @Nullable ItemStack getInactiveStack();
}