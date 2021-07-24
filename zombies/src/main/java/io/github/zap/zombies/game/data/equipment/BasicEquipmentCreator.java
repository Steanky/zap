package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of an {@link EquipmentCreator}
 */
public class BasicEquipmentCreator implements EquipmentCreator {

    private final @NotNull Map<@NotNull String, @NotNull EquipmentObjectGroupMapping> equipmentObjectGroupMappings;

    private final @NotNull Map<@NotNull String, @NotNull EquipmentMapping<@NotNull ?, @NotNull ?>> equipmentMappings = new HashMap<>();

    public BasicEquipmentCreator(@NotNull Map<@NotNull String, @NotNull EquipmentObjectGroupMapping> equipmentObjectGroupMappings,
                                 @NotNull List<@NotNull EquipmentDataMappingPair<@NotNull ?, @NotNull ?>> equipmentMappings) {
        this.equipmentObjectGroupMappings = equipmentObjectGroupMappings;

        for (@NotNull EquipmentDataMappingPair<@NotNull ?, @NotNull ?> equipmentDataMappingPair : equipmentMappings) {
            this.equipmentMappings.put(equipmentDataMappingPair.data().getType(), equipmentDataMappingPair.mapping());
        }
    }

    @Override
    public @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull String equipmentObjectGroupType,
                                                                    @NotNull OfflinePlayer player,
                                                                    @NotNull Set<Integer> slots) {
        return equipmentObjectGroupMappings.get(equipmentObjectGroupType).createEquipmentObjectGroup(player, slots);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends @NotNull EquipmentData<@NotNull ?>> @Nullable Equipment<D, @NotNull ?> createEquipment(@NotNull ZombiesPlayer player,
                                                                                                             int slot,
                                                                                                             D data) {
        @NotNull EquipmentMapping<@NotNull ?, @NotNull ?> creator = equipmentMappings.get(data.getType());
        return (creator != null) ? ((EquipmentMapping<D, @NotNull ?>) creator).createEquipment(player, slot, data) : null;
    }

}
