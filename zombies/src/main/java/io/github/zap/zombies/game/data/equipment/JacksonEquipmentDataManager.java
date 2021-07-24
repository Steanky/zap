package io.github.zap.zombies.game.data.equipment;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.SprayGunData;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunData;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeData;
import io.github.zap.zombies.game.data.equipment.perk.ExtraHealthData;
import io.github.zap.zombies.game.data.equipment.perk.ExtraWeaponData;
import io.github.zap.zombies.game.data.equipment.perk.FastReviveData;
import io.github.zap.zombies.game.data.equipment.perk.FlamingBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.FrozenBulletsData;
import io.github.zap.zombies.game.data.equipment.perk.QuickFireData;
import io.github.zap.zombies.game.data.equipment.perk.SpeedPerkData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import io.github.zap.zombies.game.util.ParticleDataWrapper;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing and managing pieces of equipment using a {@link DataLoader}
 */
public class JacksonEquipmentDataManager implements EquipmentDataManager {

    private final FieldTypeDeserializer<EquipmentData<@NotNull ?>> equipmentDataDeserializer
            = new FieldTypeDeserializer<>("type");

    private final FieldTypeDeserializer<ParticleDataWrapper<?>> particleDataWrapperDeserializer
            = new FieldTypeDeserializer<>("type") {
        {
            Map<String, Class<? extends ParticleDataWrapper<?>>> mappings = getMappings();
            mappings.put(ParticleDataWrapper.DUST_DATA_NAME, ParticleDataWrapper.DustParticleDataWrapper.class);
            mappings.put(ParticleDataWrapper.BLOCK_DATA_NAME, ParticleDataWrapper.BlockParticleDataWrapper.class);
            mappings.put(ParticleDataWrapper.ITEM_STACK_DATA_NAME,
                    ParticleDataWrapper.ItemStackParticleDataWrapper.class);
        }
    };

    private final @NotNull Map<String, Map<String, EquipmentData<@NotNull ?>>> equipmentDataMap = new HashMap<>();

    private final @NotNull DataLoader dataLoader;

    private boolean loaded = false;

    {
        // melee weapons
        addEquipmentType(EquipmentType.BASIC_MELEE.name(), BasicMeleeData.class);
        addEquipmentType(EquipmentType.AOE_MELEE.name(), AOEMeleeData.class);

        // guns
        addEquipmentType(EquipmentType.LINEAR_GUN.name(), LinearGunData.class);
        addEquipmentType(EquipmentType.SPRAY_GUN.name(), SprayGunData.class);
        addEquipmentType(EquipmentType.ZAPPER.name(), ZapperGunData.class);
        addEquipmentType(EquipmentType.GUARDIAN.name(), GuardianGunData.class);

        // perks
        addEquipmentType(EquipmentType.EXTRA_HEALTH.name(), ExtraHealthData.class);
        addEquipmentType(EquipmentType.EXTRA_WEAPON.name(), ExtraWeaponData.class);
        addEquipmentType(EquipmentType.FAST_REVIVE.name(), FastReviveData.class);
        addEquipmentType(EquipmentType.FLAMING_BULLETS.name(), FlamingBulletsData.class);
        addEquipmentType(EquipmentType.FROZEN_BULLETS.name(), FrozenBulletsData.class);
        addEquipmentType(EquipmentType.QUICK_FIRE.name(), QuickFireData.class);
        addEquipmentType(EquipmentType.SPEED.name(), SpeedPerkData.class);
    }

    public JacksonEquipmentDataManager(@NotNull DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    public <D extends EquipmentData<@NotNull L>, L> void addEquipmentType(@NotNull String equipmentType,
                                                                 @NotNull Class<D> dataClass) {
        equipmentDataDeserializer.getMappings().put(equipmentType, dataClass);
    }

    @Override
    public @Nullable EquipmentData<@NotNull ?> getEquipmentData(@NotNull String mapName, @NotNull String name) {
        if (!loaded) {
            load();
        }

        Map<String, EquipmentData<@NotNull ?>> dataForMap = equipmentDataMap.get(mapName);
        if (dataForMap != null) {
            return dataForMap.get(name);
        }
        else {
            Zombies.warning("Unable to find equipment data for " + name + " in map " + mapName);
            return null;
        }
    }

    /**
     * Loads all equipment data
     */
    private void load() {
        if (!loaded) {
            File[] files = dataLoader.getRootDirectory().listFiles();

            //TODO: probably should modify how this is loaded, it really shouldn't be done like this
            ArenaApi.getInstance().addDeserializer(EquipmentData.class, equipmentDataDeserializer);
            ArenaApi.getInstance().addDeserializer(ParticleDataWrapper.class, particleDataWrapperDeserializer);

            if (files != null) {
                for (File file : files) {
                    EquipmentDataMap newEquipmentDataMapping = dataLoader.load(FilenameUtils
                            .getBaseName(file.getName()), EquipmentDataMap.class);

                    if (newEquipmentDataMapping != null) {
                        for (Map.Entry<String, EquipmentData<@NotNull ?>> mapping :
                                newEquipmentDataMapping.getMap().entrySet()) {
                            EquipmentData<@NotNull ?> equipmentData = mapping.getValue();

                            if (equipmentData != null) {
                                equipmentDataMap.computeIfAbsent(mapping.getKey(), (String unused) -> new HashMap<>())
                                        .put(equipmentData.getName(), equipmentData);
                            }
                        }
                    }
                }
            }

            loaded = true;
        }
    }

}
