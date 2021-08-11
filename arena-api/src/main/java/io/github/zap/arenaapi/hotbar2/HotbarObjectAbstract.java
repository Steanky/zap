package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HotbarObjectAbstract implements HotbarObject {
    protected final PlayerView playerView;
    protected final int slot;

    private boolean shown = true;

    protected HotbarObjectAbstract(@NotNull PlayerView playerView, int slot) {
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
    public int getSlot() {
        return slot;
    }

    public void setStack(@Nullable ItemStack stack) {
        getOwner().getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(slot, stack));
    }
}