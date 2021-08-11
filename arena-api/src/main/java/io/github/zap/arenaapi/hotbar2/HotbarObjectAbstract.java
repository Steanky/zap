package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

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
            getOwner().getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(slot, getStack()));
            shown = true;
        }
    }

    @Override
    public void hide() {
        if(shown) {
            getOwner().getPlayerIfValid().ifPresent(player -> player.getInventory().setItem(slot, null));
            shown = false;
        }
    }

    @Override
    public int getSlot() {
        return slot;
    }
}