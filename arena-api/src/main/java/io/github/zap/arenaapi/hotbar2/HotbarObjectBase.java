package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HotbarObjectBase implements HotbarObject {
    protected final HotbarCanvas hotbarCanvas;
    protected final int slot;

    private boolean isShown = true;
    private boolean isSelected = false;
    private ItemStack currentStack;

    protected HotbarObjectBase(@NotNull HotbarCanvas hotbarCanvas, int slot) {
        this.hotbarCanvas = hotbarCanvas;
        this.slot = slot;
    }

    @Override
    public @NotNull HotbarCanvas getCanvas() {
        return hotbarCanvas;
    }

    @Override
    public boolean isShown() {
        return isShown;
    }

    @Override
    public void show() {
        if(!isShown()) {
            setStack(getStack());
            isShown = true;
        }
    }

    @Override
    public void hide() {
        if(isShown()) {
            setStack(null);
            isShown = false;
        }
    }

    @Override
    public void cleanup() {}

    @Override
    public void refresh() {
        ItemStack current = getStack();
        setStack(current);
        hotbarCanvas.drawItem(current, getSlot());
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
        this.currentStack = stack;
    }

    @Override
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();

        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handleLeftClick(event);
        }
        else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleRightClick(event);
        }
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void onSelected() {
        if(!isSelected()) {
            isSelected = true;
            handleSelect();
        }
    }

    @Override
    public void onDeselected() {
        if(isSelected()) {
            isSelected = false;
            handleDeselect();
        }
    }

    protected void handleLeftClick(@NotNull PlayerInteractEvent event) {}

    protected void handleRightClick(@NotNull PlayerInteractEvent event) {}

    protected void handleSelect() {}

    protected void handleDeselect() {}
}