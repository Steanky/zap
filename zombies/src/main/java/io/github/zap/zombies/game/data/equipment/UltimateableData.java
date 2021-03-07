package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.util.RomanNumeral;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Data for a piece of equipment which can be ultimated using the ultimate machine
 * @param <L> The type of the equipment levels
 */
public abstract class UltimateableData<L> extends EquipmentData<L> {

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (level > 0) {
            itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public String getFormattedDisplayNameWithChatColor(ChatColor chatColor, Player player, int level) {
        String formattedDisplayName = getDisplayName();
        if (level > 0) {
            formattedDisplayName = ChatColor.BOLD.toString() + formattedDisplayName;
            formattedDisplayName += " Ultimate";

            if (level > 1) {
                formattedDisplayName += " " + RomanNumeral.toRoman(level);
            }
        }

        return chatColor + formattedDisplayName;
    }

}
