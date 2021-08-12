package io.github.zap.arenaapi.hotbar2;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface HotbarCanvas {
    void drawItem(@Nullable ItemStack stack, int slot);
}
