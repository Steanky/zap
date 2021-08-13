package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class HotbarObjectBase implements HotbarObject {
    protected ItemStack currentStack;

    @Override
    public void cleanup() {}

    @Override
    public @NotNull ItemStack getStack() {
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

    protected void handleLeftClick(@NotNull PlayerInteractEvent event) {}

    protected void handleRightClick(@NotNull PlayerInteractEvent event) {}
}