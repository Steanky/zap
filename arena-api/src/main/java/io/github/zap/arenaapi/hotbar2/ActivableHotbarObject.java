package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ActivableHotbarObject extends HotbarObjectAbstract {
    protected final ItemStack inactiveStack;

    private ItemStack currentStack;
    private boolean active = false;

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
            setStack(inactiveStack);
            active = false;
        }
    }

    @Override
    public @NotNull ItemStack getStack() {
        return currentStack;
    }

    @Override
    public void setStack(@Nullable ItemStack stack) {
        super.setStack(stack);
        currentStack = stack;
    }
}
