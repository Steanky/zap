package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Data for a melee weapon
 * @param <L> The level type of the melee weapon
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
public class MeleeData<L extends MeleeLevel> extends EquipmentData<L> {

    public MeleeData(String type, String name, String displayName, Material material, List<String> lore,
                     List<L> levels) {
        super(type, name, displayName, lore, levels, material);
    }

    protected MeleeData() {

    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
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
    public TextColor getDefaultChatColor() {
        return NamedTextColor.GREEN;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.MELEE.name();
    }
}
