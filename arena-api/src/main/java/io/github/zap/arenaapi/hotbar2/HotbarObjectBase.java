package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotbarObjectBase implements HotbarObject {
    protected final HotbarManager hotbarManager;
    protected ItemStack currentStack;

    private boolean selected;

    protected HotbarObjectBase(@NotNull HotbarManager hotbarManager, @Nullable ItemStack initialStack, boolean selected) {
        this.hotbarManager = hotbarManager;
        this.currentStack = initialStack;
        this.selected = selected;

        hotbarManager.eventRegister().registerHandler(this::handlePlayerInteract, PlayerInteractEvent.class);
        hotbarManager.eventRegister().registerHandler(this::handleItemHeld, PlayerItemHeldEvent.class);
    }

    protected HotbarObjectBase(@NotNull HotbarManager hotbarManager, boolean selected) {
        this(hotbarManager, null, selected);
    }

    private void handlePlayerInteract(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();

        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            onLeftClick(event);
        }
        else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            onRightClick(event);
        }
    }

    private void handleItemHeld(@NotNull PlayerItemHeldEvent event) {
        int index = getSlot();

        if(index == event.getNewSlot()) {
            selected = true;
            onSelected(event);
        }
        else if(index == event.getPreviousSlot()) {
            selected = false;
            onDeselected(event);
        }
    }

    @Override
    public void cleanup() {}

    @Override
    public @Nullable ItemStack getStack() {
        return currentStack;
    }

    @Override
    public final boolean isSelected() {
        return selected;
    }

    public void redraw() {
        int index = getSlot();

        if(index != -1) {
            hotbarManager.redrawHotbarObject(index);
        }
    }

    public int getSlot() {
        return hotbarManager.currentProfile().getSlotFor(this);
    }

    protected void onLeftClick(@NotNull PlayerInteractEvent event) {}

    protected void onRightClick(@NotNull PlayerInteractEvent event) {}

    protected void onSelected(@NotNull PlayerItemHeldEvent event) {}

    protected void onDeselected(@NotNull PlayerItemHeldEvent event) {}
}
