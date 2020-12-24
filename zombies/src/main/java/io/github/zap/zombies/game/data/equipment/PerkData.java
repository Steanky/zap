package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.PerkLevel;
import io.github.zap.zombies.game.perk.PerkType;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {
    public PerkData(String name, String displayName, List<String> lore, List<PerkLevel> levels, Material material, PerkType perkType) {
        super(name, displayName, material, lore, levels);
    }

    private PerkData() {

    }
}
