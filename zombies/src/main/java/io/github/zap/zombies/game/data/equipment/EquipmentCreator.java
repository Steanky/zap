package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Creates {@link Equipment}s from a {@link ZombiesPlayer} and the equipment's data in a specific slot
 */
public interface EquipmentCreator {

    /**
     * Creates an equipment object group based on its equipment type
     * @param equipmentType The string representation of the equipment type
     * @param player The player to create the equipment object group for
     * @param slots The slots allocated for the equipment object group
     * @return The new equipment object group
     */
    @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull String equipmentType,
                                                             @NotNull OfflinePlayer player,
                                                             @NotNull Set<Integer> slots);

    /**
     * Creates a piece of equipment
     * @param data The data for the equipment
     * @param <D> The type of the equipment's data
     * @param <L> The type of the equipment's levels
     * @return The equipment
     */
    <D extends @NotNull EquipmentData<L>, L extends @NotNull Object> @Nullable Equipment<D, L> createEquipment(@NotNull ZombiesPlayer player,
                                                                                                               int slot,
                                                                                                               D data);

}
