package io.github.zap.zombies.game.data.equipment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores and manages information about equipments
 */
public interface EquipmentDataManager {

    /**
     * Adds a piece of equipment mapping
     * @param equipmentType The equipmentType key of the equipment type
     * @param dataClass The class of the data used to create the equipment
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used by the equipment
     */
    <D extends @NotNull EquipmentData<L>, L extends @NotNull Object> void addEquipmentType(@NotNull String equipmentType,
                                                                                           @NotNull Class<D> dataClass);
    /**
     * Gets a piece of equipment data
     * @param mapName The name of the map the equipment will be used in
     * @param name The name that the equipment data belongs to
     * @return The equipment data
     */
    @Nullable EquipmentData<@NotNull ?> getEquipmentData(@NotNull String mapName, @NotNull String name);

}
