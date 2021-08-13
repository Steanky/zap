package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HotbarObjectBase implements HotbarObject {
    protected final HotbarManager hotbarManager;
    protected ItemStack currentStack;

    protected HotbarObjectBase(@NotNull HotbarManager hotbarManager, @Nullable ItemStack initialStack) {
        this.hotbarManager = hotbarManager;
        this.currentStack = initialStack;
    }

    @Override
    public void cleanup() {}

    @Override
    public @Nullable ItemStack getStack() {
        return currentStack;
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
    public void onSelected() {

    }

    @Override
    public void onDeselected() {

    }

    /*
    this maybe needs a more performant implementation — this is just an example of a method HotbarObjectBase might
    want to provide so subclasses can update the view

    note: indexof is actually not slow, even though it's O(n) i'm 99% sure it's way faster than a hashmap
    seeing as it just iterates a fixed-length array of 9 elements
     */
    public void redraw() {
        int index = hotbarManager.currentProfile().indexOf(this);

        if(index != -1) {
            hotbarManager.redrawHotbarObject(index);
        }
    }

    protected void handleLeftClick(@NotNull PlayerInteractEvent event) {}

    protected void handleRightClick(@NotNull PlayerInteractEvent event) {}
}