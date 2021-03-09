package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {

    @Getter
    private PerkType perkType;

    private PerkData() {

    }

    @Override
    public ItemStack createItemStack(Player player, int level) {
        ItemStack itemStack = super.createItemStack(player, level);
        itemStack.setAmount(level + 1);

        return itemStack;
    }

    @Override
    public TextColor getDefaultChatColor() {
        return NamedTextColor.BLUE;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.PERK.name();
    }
}
