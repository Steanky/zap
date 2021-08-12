package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HotbarObjectBase implements HotbarObject {
    protected final PlayerView playerView;
    protected final int slot;

    private boolean shown = true;
    private ItemStack currentStack;

    protected HotbarObjectBase(@NotNull PlayerView playerView, int slot) {
        this.playerView = playerView;
        this.slot = slot;
    }

    @Override
    public @NotNull PlayerView getOwner() {
        return playerView;
    }

    @Override
    public boolean isShown() {
        return shown;
    }

    @Override
    public void show() {
        if(!shown) {
            setStack(getStack());
            shown = true;
        }
    }

    @Override
    public void hide() {
        if(shown) {
            setStack(null);
            shown = false;
        }
    }

    @Override
    public void refresh() {
        setStack(getStack());
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack getStack() {
        return currentStack;
    }

    public void setStack(@Nullable ItemStack stack) {
        if(stack != currentStack) {
            getOwner().getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(slot, currentStack = stack));
        }
    }
}