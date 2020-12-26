package io.github.zap.zombies.game.data.equipment.skill;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.level.SkillLevel;
import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<SkillLevel> {

    @Getter
    private int delay;

    public SkillData(String name, String displayName, Material material, List<String> lore, List<SkillLevel> levels, int delay) {
        super(name, displayName, material, lore, levels);

        this.delay = delay;
    }

    private SkillData() {

    }

    @Override
    public ChatColor getDefaultChatColor() {
        return ChatColor.AQUA;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.SKILL.toString();
    }

}
