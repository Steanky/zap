package io.github.zap.arenaapi.nms.v1_16_R3.itemstack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.arenaapi.nms.common.itemstack.ItemStackBridge;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackBridge_v1_16_R3 implements ItemStackBridge {

    public static final ItemStackBridge_v1_16_R3 INSTANCE = new ItemStackBridge_v1_16_R3();

    private ItemStackBridge_v1_16_R3() {}

    @Override
    public @Nullable ItemStack addNBT(@NotNull ItemStack itemStack, @NotNull String nbt) {
        try {
            NBTTagCompound parsed = MojangsonParser.parse(nbt);
            net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            nmsItemStack.setTag(parsed);

            return nmsItemStack.getBukkitStack();
        } catch (CommandSyntaxException e) {
            return null; // TODO: somehow pass this exception on?
        }
    }

}
