package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of an {@link EquipmentCreator}
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicEquipmentCreator implements EquipmentCreator {

    private final @NotNull Map<@NotNull String, @NotNull EquipmentObjectGroupMapping> equipmentObjectGroupMappings;

    private final @NotNull Map<@NotNull String, @NotNull EquipmentMapping<@NotNull ?, @NotNull ?>> equipmentMappings;

    public BasicEquipmentCreator(@NotNull Map<@NotNull String, @NotNull EquipmentObjectGroupMapping> equipmentObjectGroupMappings,
                                 @NotNull Map<@NotNull String, @NotNull EquipmentMapping<@NotNull ?, @NotNull ?>> equipmentMappings) {
        this.equipmentObjectGroupMappings = equipmentObjectGroupMappings;
        this.equipmentMappings = equipmentMappings;
    }

    @Override
    public @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull String equipmentObjectGroupType,
                                                                    @NotNull OfflinePlayer player,
                                                                    @NotNull Set<Integer> slots) {
        return equipmentObjectGroupMappings.get(equipmentObjectGroupType).createEquipmentObjectGroup(player, slots);
    }

    @SuppressWarnings("unchecked") // TODO: somehow make this not unchecked?
    @Override
    public <D extends @NotNull EquipmentData<L>, L extends @NotNull Object> @Nullable Equipment<D, L> createEquipment(@NotNull ZombiesPlayer player,
                                                                                                                      int slot,
                                                                                                                      D data) {
        @NotNull EquipmentMapping<@NotNull ?, @NotNull ?> creator = equipmentMappings.get(data.getType());
        return (creator != null) ? ((EquipmentMapping<D, L>) creator).createEquipment(player, slot, data) : null;
    }

}
