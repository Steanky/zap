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
    }

    protected HotbarObjectBase(@NotNull HotbarManager hotbarManager, boolean selected) {
        this(hotbarManager, null, selected);
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
            onLeftClick(event);
        }
        else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            onRightClick(event);
        }
    }

    @Override
    public final boolean isSelected() {
        return selected;
    }

    @Override
    public final void setSelected(@NotNull PlayerItemHeldEvent event) {
        if(!selected) {
            selected = true;
            onSelected(event);
        }
    }

    @Override
    public final void setDeselected(@NotNull PlayerItemHeldEvent event) {
        if(selected) {
            selected = false;
            onDeselected(event);
        }
    }

    public void redraw() {
        int index = hotbarManager.currentProfile().indexOf(this);

        if(index != -1) {
            hotbarManager.redrawHotbarObject(index);
        }
    }

    protected void onLeftClick(@NotNull PlayerInteractEvent event) {}

    protected void onRightClick(@NotNull PlayerInteractEvent event) {}

    protected void onSelected(@NotNull PlayerItemHeldEvent event) {}

    protected void onDeselected(@NotNull PlayerItemHeldEvent event) {}
}
