package io.github.zap.zombies.game.data.equipment.melee;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Data for a melee weapon
 */
public class MeleeData extends EquipmentData<MeleeLevel> {
    public MeleeData(String name, String displayName, Material material, List<String> lore, List<MeleeLevel> levels) {
        super(name, displayName, material, lore, levels);
    }
    private MeleeData() {

    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);

        MeleeLevel meleeLevel = getLevels().get(level);
        for (ImmutablePair<Enchantment, Integer> pair : meleeLevel.getEnchantments()) {
            itemMeta.addEnchant(pair.left, pair.right, true);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public ChatColor getDefaultChatColor() {
        return ChatColor.GREEN;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.MELEE.toString();
    }
}
