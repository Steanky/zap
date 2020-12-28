package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.serialize.JacksonDataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.skill.SkillData;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import io.github.zap.zombies.game.equipment.EquipmentObjectGroupCreator;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.equipment.gun.LinearGun;
import io.github.zap.zombies.game.equipment.melee.MeleeWeapon;
import io.github.zap.zombies.game.equipment.perk.PerkEquipment;
import io.github.zap.zombies.game.equipment.skill.SkillEquipment;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for storing and managing pieces of equipment using Jackson's data loader
 */
@RequiredArgsConstructor
public class JacksonEquipmentManager implements EquipmentManager {

    private final EquipmentDataDeserializer equipmentDataDeserializer = new EquipmentDataDeserializer();

    private final EquipmentCreator equipmentCreator = new EquipmentCreator();

    private final EquipmentObjectGroupCreator equipmentObjectGroupCreator = new EquipmentObjectGroupCreator();

    private final Map<String, Map<String, EquipmentData<?>>> equipmentDataMap = new HashMap<>();

    private final File dataFolder;

    private boolean loaded = false;

    {
        addEquipmentType(EquipmentType.MELEE.name(), MeleeData.class, MeleeWeapon::new);
        addEquipmentType(EquipmentType.SKILL.name(), SkillData.class, SkillEquipment::new);
        addEquipmentType(EquipmentType.PERK.name(), PerkData.class, PerkEquipment::new);
        addEquipmentType(EquipmentType.LINEAR_GUN.name(), LinearGunData.class, LinearGun::new);
    }

    public <D extends EquipmentData<L>, L> void  addEquipmentType(String equipmentType, Class<D> dataClass,
                     EquipmentCreator.EquipmentMapping<D, L> equipmentMapping) {
        equipmentDataDeserializer.getEquipmentDataClassMappings().put(equipmentType, dataClass);
        equipmentCreator.getEquipmentMappings().put(equipmentType, equipmentMapping);
    }

    @Override
    public EquipmentData<?> getEquipmentData(String mapName, String name) {
        if (!loaded) {
            load();
        }

        return equipmentDataMap.get(mapName).get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, String mapName,
                                                                           String name) {
        return createEquipment(player, slot, (D) getEquipmentData(mapName, name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, D equipmentData) {
        EquipmentCreator.EquipmentMapping<D, L> equipmentMapping = (EquipmentCreator.EquipmentMapping<D, L>)
                equipmentCreator.getEquipmentMappings().get(equipmentData.getEquipmentType());

        return equipmentMapping.createEquipment(player, slot, equipmentData);
    }

    @Override
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
            JacksonDataLoader dataLoader = (JacksonDataLoader) Zombies.getInstance().getDataLoader();
            dataLoader.addDeserializer(EquipmentData.class, equipmentDataDeserializer);

            if (files != null) {
                for (File file : files) {
                    EquipmentDataMap newEquipmentDataMapping = dataLoader.load(file, EquipmentDataMap.class);

                    if (newEquipmentDataMapping != null) {
                        for (Map.Entry<String, EquipmentData<?>> mapping :
                                newEquipmentDataMapping.getMap().entrySet()) {
                            EquipmentData<?> equipmentData = mapping.getValue();
                            equipmentDataMap.computeIfAbsent(
                                    mapping.getKey(),(String unused) -> new HashMap<>()
                            ).put(equipmentData.getName(), equipmentData);
                        }
                    }
                }
            }

            loaded = true;
        }
    }

}
