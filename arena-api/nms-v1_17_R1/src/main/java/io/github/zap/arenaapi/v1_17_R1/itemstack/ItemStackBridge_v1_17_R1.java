package io.github.zap.arenaapi.v1_17_R1.itemstack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.arenaapi.nms.common.itemstack.ItemStackBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackBridge_v1_17_R1 implements ItemStackBridge {

    public static final ItemStackBridge_v1_17_R1 INSTANCE = new ItemStackBridge_v1_17_R1();

    private ItemStackBridge_v1_17_R1() {}

    @Override
    public @Nullable ItemStack addNBT(@NotNull ItemStack itemStack, @NotNull String nbt) {
        try {
            CompoundTag parsed = TagParser.parseTag(nbt);
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            nmsItemStack.setTag(parsed);

            return nmsItemStack.getBukkitStack();
        } catch (CommandSyntaxException e) {
            return null; // TODO: somehow pass this exception on?
        }
    }

}
