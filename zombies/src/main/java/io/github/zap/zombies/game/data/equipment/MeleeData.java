package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.MeleeLevel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MeleeData extends EquipmentData<MeleeLevel> {
    public MeleeData(String displayName, Material material, List<String> lore, List<MeleeLevel> levels) {
        super(displayName, material, lore, levels);
    }
    private MeleeData() {

    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public String getFormattedDisplayName(int level, String displayName) {
        return ChatColor.BLUE.toString() + super.getFormattedDisplayName(level, displayName);
    }
}
