package io.github.zap.nms.common.itemstack;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A bridge for methods relating to item stacks
 */
public interface ItemStackBridge {

    /**
     * Adds nbt to a copy of an item stack
     * @param itemStack The item stack to copy
     * @param nbt The nbt to add
     * @return The new item stack with nbt
     */
    @Nullable ItemStack addNBT(@NotNull ItemStack itemStack, @NotNull String nbt);

}
