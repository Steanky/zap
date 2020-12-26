package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Stores and manages information about equipments
 */
public interface EquipmentManager {

    /**
     * Adds a piece of equipment mapping
     * @param name The name key of the equipment
     * @param dataClass The class of the data used to create the equipment
     * @param equipmentMapping A mapping class used to create the equipment from a data instance
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used by the equipment
     */
    <D extends EquipmentData<L>, L> void addEquipment(String name, Class<D> dataClass,
                                                             EquipmentCreator.EquipmentMapping<D, L> equipmentMapping);

    /**
     * Gets a piece of equipment data
     * @param name The name that the equipment dat belongs to
     * @return The equipment data
     */
    EquipmentData<?> getEquipmentData(String name);

    /**
     * Creates a piece of equipment
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param name The name key of the equipment
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @return The new piece of equipment
     */
    <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, String name);

    /**
     * Creates a piece of equipment
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param equipmentData The equipment data to create the equipment with
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @return The new piece of equipment
     */
    <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, D equipmentData);

    /**
     * Creates an equipment object group based on its equipment type
     * @param equipmentType The string representation of the equipment type
     * @param player The player to create the equipment object group for
     * @param slots The slots allocated for the equipment object group
     * @return The new equipment object group
     */
    EquipmentObjectGroup createEquipmentObjectGroup(String equipmentType, Player player, Set<Integer> slots);

}
