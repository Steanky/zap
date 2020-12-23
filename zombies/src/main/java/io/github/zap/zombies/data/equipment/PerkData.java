package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.level.PerkLevel;

import java.util.List;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {
    public PerkData(String name, String displayName, List<String> lore, List<PerkLevel> levels, String materialName) {
        super(name, displayName, materialName, lore, levels);
    }
}
