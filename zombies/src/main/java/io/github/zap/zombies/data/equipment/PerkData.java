package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.level.PerkLevel;
import io.github.zap.zombies.game.perk.PerkType;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a perk
 */
public class PerkData extends EquipmentData<PerkLevel> {

    @Getter
    private final PerkType perkType;

    public PerkData(String name, String displayName, List<String> lore, List<PerkLevel> levels, Material material, PerkType perkType) {
        super(name, displayName, material, lore, levels);

        this.perkType = perkType;
    }
}
