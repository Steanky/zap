package io.github.zap.zombies.game.equipment.skill;

import io.github.zap.zombies.game.data.equipment.SkillData;
import io.github.zap.zombies.game.equipment.Equipment;
import org.bukkit.entity.Player;

/**
 * Represents a skill
 */
public class Skill extends Equipment<SkillData> {
    public Skill(Player player, int slotId, SkillData equipmentData) {
        super(player, slotId, equipmentData);
    }
}
