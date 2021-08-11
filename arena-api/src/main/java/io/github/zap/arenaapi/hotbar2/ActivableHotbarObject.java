package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ActivableHotbarObject extends HotbarObjectAbstract {
    private boolean active = true;
    private final ItemStack inactiveStack;
    protected ItemStack currentStack;

    protected ActivableHotbarObject(@NotNull PlayerView playerView, @NotNull ItemStack inactiveStack, int slot) {
        super(playerView, slot);
        this.inactiveStack = currentStack = inactiveStack;
    }

    @Override
    public void activate() {
        if(!active) {
            setStack(currentStack);
            active = true;
        }
    }

    @Override
    public void deactivate() {
        if(active) {
            currentStack = inactiveStack;
            setStack(currentStack);
            active = false;
        }
    }

    @Override
    public @NotNull ItemStack getStack() {
        return currentStack;
    }
}
