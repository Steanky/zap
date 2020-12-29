package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {

    @Getter
    private PerkType perkType;

    private PerkData() {

    }

    @Override
    public ChatColor getDefaultChatColor() {
        return ChatColor.BLUE;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.PERK.name();
    }
}
