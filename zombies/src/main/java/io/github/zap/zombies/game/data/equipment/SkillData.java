package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.PerkLevel;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<PerkLevel> {

    @Getter
    private int delay;

    public SkillData(String name, String displayName, Material material, List<String> lore, List<PerkLevel> levels, int delay) {
        super(name, displayName, material, lore, levels);

        this.delay = delay;
    }

    private SkillData() {

    }

}
