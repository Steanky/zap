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
import lombok.Getter;
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

    private final Map<String, EquipmentData<?>> equipmentDataMap = new HashMap<>();

    private final File dataFolder;

    private boolean loaded = false;

    {
        addEquipment(EquipmentType.MELEE.name(), MeleeData.class, MeleeWeapon::new);
        addEquipment(EquipmentType.SKILL.name(), SkillData.class, SkillEquipment::new);
        addEquipment(EquipmentType.PERK.name(), PerkData.class, PerkEquipment::new);
        addEquipment(EquipmentType.LINEAR_GUN.name(), LinearGunData.class, LinearGun::new);
    }

    public <D extends EquipmentData<L>, L> void addEquipment(String equipmentType, Class<D> dataClass,
                                                             EquipmentCreator.EquipmentMapping<D, L> equipmentMapping) {
        equipmentDataDeserializer.getEquipmentDataClassMappings().put(equipmentType, dataClass);
        equipmentCreator.getEquipmentMappings().put(equipmentType, equipmentMapping);
    }

    @Override
    public EquipmentData<?> getEquipmentData(String name) {
        if (!loaded) {
            load();
        }

        return equipmentDataMap.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends EquipmentData<L>, L> Equipment<D, L> createEquipment(Player player, int slot, String name) {
        return createEquipment(player, slot, (D) getEquipmentData(name));
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
