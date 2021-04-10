package io.github.zap.zombies.game.data.equipment.skill;

import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupType;
import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for a skill
 */
public class SkillData extends EquipmentData<SkillLevel> {

    @Getter
    private int delay;

    public SkillData(String name, String displayName, Material material, List<String> lore, List<SkillLevel> levels,
                     int delay) {
        super(name, displayName, material, lore, levels);

        this.delay = delay;
    }

    private SkillData() {

    }

    @Override
    public @NotNull TextColor getDefaultChatColor() {
        return NamedTextColor.AQUA;
    }

    @Override
    public @NotNull String getEquipmentType() {
        return EquipmentType.SKILL.name();
    }

    @Override
    public @NotNull String getEquipmentObjectGroupType() {
        return EquipmentObjectGroupType.SKILL.name();
    }

}
