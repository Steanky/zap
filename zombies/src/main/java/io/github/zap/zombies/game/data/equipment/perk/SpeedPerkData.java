package io.github.zap.zombies.game.data.equipment.perk;

import io.github.zap.zombies.game.equipment.EquipmentType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Data for the speed perk
 */
@SuppressWarnings("FieldMayBeFinal")
public class SpeedPerkData extends PerkData<@NotNull SpeedPerkLevel> {

    private int speedReapplyInterval = 500;

    public SpeedPerkData(@NotNull String name, @NotNull String displayName, @NotNull Material material,
                         @NotNull List<String> lore, @NotNull List<SpeedPerkLevel> levels) {
        super(EquipmentType.SPEED.name(), name, displayName, material, lore, levels);
    }

    private SpeedPerkData() {

    }

    /**
     * Gets the interval at which speed should be reapplied
     * @return The interval
     */
    public int getSpeedReapplyInterval() {
        return speedReapplyInterval;
    }

}
