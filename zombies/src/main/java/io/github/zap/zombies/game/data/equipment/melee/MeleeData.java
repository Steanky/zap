package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Data for a melee weapon
 * @param <L> The level type of the melee weapon
 */
@Getter
public abstract class MeleeData<L extends MeleeLevel> extends EquipmentData<L> {

    protected MeleeData() {

    }

    @Override
    public @NotNull ItemStack createItemStack(@NotNull Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);

        L meleeLevel = getLevels().get(level);
        for (MeleeLevel.EnchantmentLevel enchantmentLevel : meleeLevel.getEnchantments()) {
            itemMeta.addEnchant(enchantmentLevel.getEnchantment(), enchantmentLevel.getLevel(), true);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public @NotNull TextColor getDefaultChatColor() {
        return NamedTextColor.GREEN;
    }

    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.MELEE.name();
    }

}
