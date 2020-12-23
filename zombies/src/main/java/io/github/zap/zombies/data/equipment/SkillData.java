package io.github.zap.zombies.data.equipment;

import io.github.zap.zombies.data.level.SkillLevel;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<SkillLevel> {

    public SkillData(String name, String displayName, String materialName, List<String> lore, List<SkillLevel> levels) {
        super(name, displayName, materialName, lore, levels);
    }

}
