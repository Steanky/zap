package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.equipment.Equipment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class EquipmentManager {

    @Getter
    private final EquipmentDeserializer equipmentDeserializer = new EquipmentDeserializer();

    @Getter
    private final EquipmentCreator equipmentCreator = new EquipmentCreator();

    private final Map<String, EquipmentData<?>> equipmentDataMap = new HashMap<>();

    private final File dataFolder;

    private boolean loaded = false;

    public <D extends EquipmentData<L>, L> void addEquipment(String name, Class<D> dataClass, EquipmentCreator.EquipmentMapping<D, L> equipmentMapping) {
        equipmentDeserializer.getEquipmentClassMappings().put(name, dataClass);
        equipmentCreator.getEquipmentMappings().put(name, equipmentMapping);
    }

    public EquipmentData<?> getEquipmentData(String name) {
        if (!loaded) {
            load();
        }

        return equipmentDataMap.get(name);
    }

    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slotId, String name) {
        D equipmentData = (D) getEquipmentData(name);
        EquipmentCreator.EquipmentMapping<D, L> equipmentMapping = (EquipmentCreator.EquipmentMapping<D, L>) equipmentCreator.getEquipmentMappings().get(equipmentData.getEquipmentType());

        return equipmentMapping.createEquipment(player, slotId, equipmentData);
    }

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
