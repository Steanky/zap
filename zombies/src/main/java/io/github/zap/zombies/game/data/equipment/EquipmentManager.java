package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for storing and managing pieces of equipment
 */
@RequiredArgsConstructor
public class EquipmentManager {

    @Getter
    private final EquipmentDeserializer equipmentDeserializer = new EquipmentDeserializer();

    @Getter
    private final EquipmentCreator equipmentCreator = new EquipmentCreator();

    @Getter
    private final EquipmentObjectGroupCreator equipmentObjectGroupCreator = new EquipmentObjectGroupCreator();

    private final Map<String, EquipmentData<?>> equipmentDataMap = new HashMap<>();

    private final File dataFolder;

    private boolean loaded = false;

    /**
     * Adds a piece of equipment mapping
     * @param name The name key of the equipment
     * @param dataClass The class of the data used to create the equipment
     * @param equipmentMapping A mapping class used to create the equipment from a data instance
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used by the equipment
     */
    public <D extends EquipmentData<L>, L> void addEquipment(String name, Class<D> dataClass,
                                                             EquipmentCreator.EquipmentMapping<D, L> equipmentMapping) {
        equipmentDeserializer.getEquipmentClassMappings().put(name, dataClass);
        equipmentCreator.getEquipmentMappings().put(name, equipmentMapping);
    }

    /**
     * Gets a piece of equipment data
     * @param name The name that the equipment dat belongs to
     * @return The equipment data
     */
    public EquipmentData<?> getEquipmentData(String name) {
        if (!loaded) {
            load();
        }

        return equipmentDataMap.get(name);
    }

    /**
     * Creates a piece of equipment
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param name The name key of the equipment
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @return The new piece of equipment
     */
    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, String name) {
        return createEquipment(player, slot, (D) getEquipmentData(name));
    }

    /**
     * Creates a piece of equipment
     * @param player The player to create the equipment for
     * @param slot The slot the equipment will go in
     * @param equipmentData The equipment data to create the equipment with
     * @param <D> The type of the data used for the equipment
     * @param <L> The type of the levels used for the equipment
     * @return The new piece of equipment
     */
    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, D equipmentData) {
        EquipmentCreator.EquipmentMapping<D, L> equipmentMapping = (EquipmentCreator.EquipmentMapping<D, L>)
                equipmentCreator.getEquipmentMappings().get(equipmentData.getEquipmentType());

        return equipmentMapping.createEquipment(player, slot, equipmentData);
    }

    /**
     * Creates an equipment object group based on its equipment type
     * @param equipmentType The string representation of the equipment type
     * @param player The player to create the equipment object group for
     * @param slots The slots allocated for the equipment object group
     * @return The new equipment object group
     */
    public EquipmentObjectGroup createEquipmentObjectGroup(String equipmentType, Player player, Set<Integer> slots) {
        return equipmentObjectGroupCreator.getEquipmentObjectGroupMappings().get(equipmentType)
                .createEquipmentObjectGroup(player, slots);
    }

    /**
     * Loads all equipment data
     */
    private void load() {
        if (!loaded) {
            //noinspection ResultOfMethodCallIgnored
            dataFolder.mkdirs();

            File[] files = dataFolder.listFiles();
            DataLoader dataLoader = Zombies.getInstance().getDataLoader();

            if (files != null) {
                for (File file : files) {
                    EquipmentData<?> equipmentData = dataLoader.load(file, EquipmentData.class);

                    if (equipmentData != null) {
                        equipmentDataMap.put(equipmentData.getName(), equipmentData);
                    }
                }
            }

            loaded = true;
        }
    }

}
