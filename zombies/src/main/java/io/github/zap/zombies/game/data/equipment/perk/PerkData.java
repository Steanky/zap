package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {
    public PerkData(String name, String displayName, List<String> lore, List<PerkLevel> levels, Material material) {
        super(EquipmentType.PERK.toString(), name, displayName, material, lore, levels);
    }

    private PerkData() {

    }

    @Override
    public ChatColor getDefaultChatColor() {
        return ChatColor.BLUE;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.PERK.toString();
    }
}
