package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.level.SkillLevel;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<SkillLevel> {

    public SkillData(String name, String displayName, Material material, List<String> lore, List<SkillLevel> levels) {
        super(name, displayName, material, lore, levels);
    }

}
