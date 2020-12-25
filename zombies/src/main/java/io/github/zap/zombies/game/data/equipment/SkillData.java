package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.PerkLevel;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<PerkLevel> {

    @Getter
    private int delay;

    public SkillData(String displayName, Material material, List<String> lore, List<PerkLevel> levels, int delay) {
        super(displayName, material, lore, levels);

        this.delay = delay;
    }

    private SkillData() {

    }

    @Override
    public String getFormattedDisplayName(int level, String displayName) {
        return ChatColor.AQUA.toString() + super.getFormattedDisplayName(level, displayName);
    }
}
