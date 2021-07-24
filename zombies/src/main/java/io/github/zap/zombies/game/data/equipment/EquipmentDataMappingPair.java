package io.github.zap.zombies.game.data.equipment;

import org.jetbrains.annotations.NotNull;

/**
 * A pair of an {@link EquipmentData} and a {@link EquipmentMapping} that creates an associated {@link io.github.zap.zombies.game.equipment.Equipment}
 * @param <D> The type of the equipment data
 * @param <M> The type of the mapping
 */
public record EquipmentDataMappingPair<D extends @NotNull EquipmentData<@NotNull ?>, M extends @NotNull EquipmentMapping<D>>(D data, M mapping) {

}
