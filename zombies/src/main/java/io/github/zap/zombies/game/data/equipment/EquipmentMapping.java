package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Mapping to create {@link Equipment}s from its data
 * @param <D> The data type of the equipment
 * @param <L> The level type of the equipment
 */
@FunctionalInterface
public interface EquipmentMapping<D extends @NotNull EquipmentData<L>, L extends @NotNull Object> {

    /**
     * Creates a piece of equipment
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will be in
     * @param data The data used for the equipment
     * @return The new piece of equipment
     */
    @NotNull Equipment<D, L> createEquipment(@NotNull ZombiesPlayer player, int slot, D data);

}
