package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classes that need to be serialized and deserialized should inherit from this.
 */
public abstract class DataSerializable implements ConfigurationSerializable {
    private static final Map<String, ValueConverter> converters = new HashMap<>();

    /**
     * Registers a ValueConverter that can be used to transform the values of fields during serialization and
     * deserialization.
     * @param name The name of the converter
     * @param converter The converter
     */
    public static void registerConverter(String name, ValueConverter converter){
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(converter, "converter cannot be null");

        converters.put(name, converter);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();

        SerializeUtil.forEachSerializable(this.getClass(), (field, name) -> {
            try {
                String serializedName = name.equals(StringUtils.EMPTY) ? field.getName() : name;
                serializedData.put(serializedName, field.get(this));
            } catch (IllegalAccessException e) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting " +
                                "to serialize field '%s' in object '%s': %s", field.toGenericString(),
                        this.toString(), e.getMessage()));
            }
        });

        return serializedData;
    }

    public static DataSerializable deserialize(Map<String, Object> data) {
        // get the classname from the type key, which will be the class we want to construct
        String className = (String) data.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);

        if (className != null) {
            try {
                return createDeserialized(className, data);
            } catch (ClassNotFoundException ignored) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' cannot be found", className));
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                    InvocationTargetException e) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("An error occured when trying to " +
                        "instantiate '%s': %s", className, e.getMessage()));
            }
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("The serialized data does not contain " +
                    "required type key '%s'", ConfigurationSerialization.SERIALIZED_TYPE_KEY));
        }

        return null;
    }

    /**
     * Creates an actual deserialized object
     * @param className The name of the class to deserialize
     * @param data The data to deserialize with
     * @return The deserialized data
     * @throws ClassNotFoundException Invalid class name
     * @throws NoSuchMethodException General deserialization error
     * @throws IllegalAccessException General deserialization error
     * @throws InvocationTargetException General deserialization error
     * @throws InstantiationException General deserialization error
     */
    private static DataSerializable createDeserialized(String className, Map<String, Object> data) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // get the class from the classname
        Class<?> instanceClass = Class.forName(className);

        if(DataSerializable.class.isAssignableFrom(instanceClass)) { //check if our instance class is DataSerializable
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

            if(!constructor.isAccessible()) {
                constructor.setAccessible(true); //required parameterless constructor can be private
            }

            Object instanceObject = constructor.newInstance(); //instantiate the object
            SerializeUtil.forEachSerializable(instanceClass, (field, name) -> setDeserializedField(instanceObject, data, name, field));

            return (DataSerializable) instanceObject;
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' is not an instance of " +
                    "DataSerializable", className));
        }

        return null;
    }

    /**
     * Sets the field of a deserialized object
     * @param instanceObject The object that will be deserialized
     * @param data The serialized data to modify the object
     * @param name The name of the field in the serialized representation
     * @param field The field to set in the object
     */
    private static void setDeserializedField(Object instanceObject, Map<String, Object> data, String name, Field field) {
        String serializedName = name.equals(StringUtils.EMPTY) ? field.getName() : name;

        try {
            Object rawValue = data.get(serializedName); // get the serialized data
            Class<?> fieldType = field.getType();

            // try to modify the value that we get from ConfigurationSerialization
            if (rawValue instanceof ArrayList && fieldType.isArray()) {
                // workaround for ConfigurationSerialization giving us arraylists when we need arrays
                rawValue = toArrayDeep((ArrayList<?>)rawValue, fieldType);

                // TODO: make it so that lists of arrays deserialize into lists of arrays instead of lists of lists
            }

            field.set(instanceObject, rawValue);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception " +
                            "when attempting to serialize field '%s' in object '%s': %s",
                    field.toGenericString(), data.toString(), e.getMessage()));
        }
    }

    // recursive utility function: deep-converts arraylists into arrays (handles any 'dimension' of arraylist)
    private static Object toArrayDeep(ArrayList<?> arrayList, Class<?> fieldType) {
        Class<?> fieldComponent = fieldType.getComponentType();

        Object[] array = arrayList.toArray();
        Object newArray = Array.newInstance(fieldComponent, arrayList.size());

        for(int i = 0; i < array.length; i++) {
            Object item = array[i];
            if(item instanceof ArrayList) {
                item = toArrayDeep((ArrayList<?>)item, fieldComponent);
            }

            Array.set(newArray, i, item);
        }

        return newArray;
    }
}