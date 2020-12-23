package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.data.level.MeleeLevel;
import org.bukkit.Material;

import java.util.List;

public class MeleeData extends EquipmentData<MeleeLevel> {
    public MeleeData(String name, String displayName, Material material, List<String> lore, List<MeleeLevel> levels) {
        super(name, displayName, material, lore, levels);
    }
}
