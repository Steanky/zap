package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores a map of string names and an equipment mapping in order to create equipments from an equipment data
 */
public class EquipmentCreator {

    @Getter
    private final Map<String, EquipmentMapping<?, ?>> equipmentMappings = new HashMap<>();

    /**
     * Interface to create a piece of equipment from its respective data class
     * @param <D> The data class of the equipment
     * @param <L> The level class of the equipment
     */
    public interface EquipmentMapping<D extends EquipmentData<L>, L> {
        /**
         * Creates a new piece of equipment
         * @param player The player to create the equipment for
         * @param slot The slot the equipment will go in
         * @param equipmentData The equipment data to create the equipment with
         * @return The new equipment
         */
        Equipment<D, L> createEquipment(Player player, int slot, D equipmentData);
    }

}
