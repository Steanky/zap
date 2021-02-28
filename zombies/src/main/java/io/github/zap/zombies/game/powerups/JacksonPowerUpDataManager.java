package io.github.zap.zombies.game.powerups;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.game.ZombiesArena;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;

public class JacksonPowerUpDataManager implements PowerUpDataManager, SupportEagerLoading {
    private final FieldTypeDeserializer<PowerUpData> equipmentDataDeserializer = new FieldTypeDeserializer<>("type");
    private final Map<String, PowerUpData> dataMap = new HashMap<>();
    private final Map<String, ImmutablePair<BiFunction<ZombiesArena, PowerUpData, PowerUp>, Class<? extends PowerUpData>>> typeMap = new HashMap<>();


    @Getter
    private boolean loaded;

    @Getter
    private final DataLoader dataLoader;

    @Getter
    private final JacksonPowerUpDataManagerOptions options;

    public JacksonPowerUpDataManager(DataLoader dataLoader, JacksonPowerUpDataManagerOptions options) {
        this.dataLoader = dataLoader;
        this.options = options;
        ArenaApi.getInstance().addDeserializer(PowerUpData.class, equipmentDataDeserializer);
        addDataLoader(PowerUpDataType.BASIC);
        loaded = !options.isLoadDefaults();
    }

    public void addDataLoader(PowerUpDataTypeLinker dataTypeLinker) {
        ensureLoad();
        equipmentDataDeserializer.getMappings().putIfAbsent(dataTypeLinker.getName(), dataTypeLinker.getDataType());
    }


    @Override
    public Set<PowerUpData> getDataSet() {
        ensureLoad();
        return new HashSet<>(dataMap.values());
    }

    @Override
    public void addPowerUpData(PowerUpData data) {
        addPowerUpData(data, true);
    }

    public void addPowerUpData(PowerUpData data, boolean throwOnError) {
        try {
            ensureLoad();
            Validate.isTrue(!dataMap.containsKey(data.getName()), data.getName() + " is already exist!");
            Validate.isTrue(!typeMap.containsKey(data.getPowerUpType()), "Cannot find type: " + data.getPowerUpType() + "! Make sure you register power-up type before adding data.");
            Validate.isAssignableFrom(typeMap.get(data.getPowerUpType()).right, data.getClass(), "The provided power up type does not accept this data type");
            dataMap.put(data.getName(), data);
        } catch (IllegalArgumentException e) {
            if(throwOnError) throw e;
        }
    }

    @Override
    public void removePowerUpsData(String name) {
        ensureLoad();
        dataMap.remove(name);
    }

    @Override
    public Set<ImmutablePair<BiFunction<ZombiesArena, PowerUpData, PowerUp>, Class<? extends PowerUpData>>> getPowerUpInitializers() {
        ensureLoad();
        return new HashSet<>(typeMap.values());
    }

    @Override
    public void registerPowerUp(String name, BiFunction<ZombiesArena, PowerUpData, PowerUp> powerUpsInitializer, Class<? extends PowerUpData> dataClass) {
        ensureLoad();
        Validate.isTrue(!typeMap.containsKey(name), name + " is already exist!");
        typeMap.put(name, ImmutablePair.of(powerUpsInitializer, dataClass));
    }

    @Override
    public void registerPowerUp(String name, Class<? extends PowerUp> classType) {
        BiFunction<ZombiesArena, PowerUpData, PowerUp> initializer = null;
        Class<? extends PowerUpData> dataClass = null;

        if(typeMap.containsKey(name)) {
            for (Constructor<?> ctor : classType.getConstructors()) {
                var params = ctor.getParameterTypes();
                if(params.length > 2 && PowerUpData.class.isAssignableFrom(params[0]) && ZombiesArena.class.isAssignableFrom(params[1])) {
                    initializer = (data, arena) -> {
                        try {
                            return (PowerUp) ctor.newInstance(data, arena);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    };

                    //noinspection unchecked
                    dataClass = (Class<? extends PowerUpData>) params[0];
                }
                break;
            }
        } else {
            throw new IllegalArgumentException(name + " is already exist!");
        }

        if(initializer != null) {
            registerPowerUp(name, initializer, dataClass);
        }
    }

    @Override
    public void registerPowerUp(Class<? extends PowerUp> classType) {
        for (AnnotatedType at : classType.getAnnotatedInterfaces()) {
            if(at instanceof PowerUpType) {
                registerPowerUp(((PowerUpType) at).getName(), classType);
                return;
            }
        }

        throw new IllegalArgumentException(classType + " does not have a defined @" + PowerUpType.class);
    }

    @Override
    public void unregisterPowerUp(String name) {
        ensureLoad();
        typeMap.remove(name);
        if(dataMap.values().stream().anyMatch(x -> x.getPowerUpType().equals(name))) {
            throw new IllegalStateException("Cannot remove power up type " + name + " because some power up data still depend on it!");
        }
    }

    @Override
    public PowerUp createPowerUp(String name, ZombiesArena arena) {
        ensureLoad();
        Validate.isTrue(dataMap.containsKey(name), name + " power up does not exist");
        var data = dataMap.get(name);
        return typeMap.get(data.getPowerUpType()).left.apply(arena, data);
    }

    @Override
    public void load() {
        if(!isLoaded()) {
            // There is no way to retrieve class from package easily so I gonna manually do it
            registerPowerUp(MaxAmmoPowerUp.class);


            //noinspection ConstantConditions
            Arrays.stream(dataLoader.getRootDirectory().listFiles())
                    .map(x -> dataLoader.load(FilenameUtils.getBaseName(x.getName()), PowerUpData.class))
                    .filter(Objects::nonNull)
                    .forEach(x -> addPowerUpData(x, false));

            loaded = true;
        }
    }

    private void ensureLoad() {
        if(!isLoaded()) {
            load();
        }
    }
}
