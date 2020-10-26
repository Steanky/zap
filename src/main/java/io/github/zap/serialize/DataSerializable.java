package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

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

        forEachSerializable(getClass(), (triple) -> {
            try {
                serializedData.put(triple.left, triple.right.convert(triple.middle.get(this), Direction.SERIALIZE));
            } catch (IllegalAccessException | IllegalArgumentException e) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting " +
                                "to serialize field '%s' in object '%s': %s", triple.middle.toGenericString(),
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
    private static DataSerializable createDeserialized(String className, Map<String, Object> data)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        // get the class from the classname
        Class<?> instanceClass = Class.forName(className);

        if(DataSerializable.class.isAssignableFrom(instanceClass)) { //check if our instance class is DataSerializable
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

            if(!constructor.isAccessible()) {
                constructor.setAccessible(true); //required parameterless constructor can be private
            }

            Object instanceObject = constructor.newInstance(); //instantiate the object
            forEachSerializable(instanceClass, (triple) -> setField(triple.middle, instanceObject,
                    triple.right.convert(data.get(triple.left), Direction.DESERIALIZE)));

            return (DataSerializable) instanceObject;
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' is not an instance of " +
                    "DataSerializable", className));
        }

        return null;
    }

    /**
     * Sets the field of a deserialized object, and outputs a warning message if it fails. Performs automatic conversion
     * between lists and arrays of all dimensions.
     * @param field The field to set in the object
     * @param instanceObject The object that will be deserialized
     * @param assignedObject The object that will be assigned
     */
    private static void setField(Field field, Object instanceObject, Object assignedObject) {
        try {
            Class<?> fieldType = field.getType();

            // try to modify the value that we get from ConfigurationSerialization
            if (assignedObject instanceof Collection && fieldType.isArray()) {
                // workaround for ConfigurationSerialization giving us arraylists when we need arrays
                assignedObject = toArrayDeep((Collection<?>)assignedObject, fieldType);
            }

            field.set(instanceObject, assignedObject);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting to assign value " +
                    "'%s' to field '%s' in object '%s': '%s'", assignedObject.toString(), field.toGenericString(),
                    instanceObject.toString(), e.getMessage()));
        }
    }

    /**
     * Recursive utility function that converts a collection into an array that may be multidimensional.
     * @param collection The collection to convert
     * @param arrayType The class of the array we are converting to
     * @return The array, which may be multidimensional
     */
    private static Object toArrayDeep(Collection<?> collection, Class<?> arrayType) {
        Class<?> arrayComponent = arrayType.getComponentType();

        Object[] array = collection.toArray();
        Object newArray = Array.newInstance(arrayComponent, collection.size());

        for(int i = 0; i < array.length; i++) {
            Object item = array[i];

            if(item instanceof Collection) {
                item = toArrayDeep((Collection<?>)item, arrayComponent);
            }

            Array.set(newArray, i, item);
        }

        return newArray;
    }

    /**
     * Runs method on every field of a class that should be serializable
     * @param clazz The class to run the method on
     * @param consumer A Consumer that takes an ImmutableTriple containing the name of the value (which may be either
     *                 the field name or the name parameter of the @Serialize annotation), the field that we are dealing
     *                 with, and a ValueConverter that will default to a converter which does nothing.
     */
    private static void forEachSerializable(Class<?> clazz, Consumer<ImmutableTriple<String, Field, ValueConverter>> consumer) {
        Field[] fields = clazz.getDeclaredFields();

        fields:
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) { // skip static fields
                Annotation[] annotations = field.getDeclaredAnnotations();

                Serialize serializeAnnotation = null;

                for (Annotation annotation : annotations) { //look for Serialize annotation
                    if(annotation instanceof Serialize) {
                        serializeAnnotation = (Serialize)annotation;
                    }
                    else if(annotation instanceof NoSerialize) {
                        continue fields;
                    }
                }

                String name;
                ValueConverter converter;
                if(serializeAnnotation != null) {
                    name = serializeAnnotation.name();
                    converter = converters.getOrDefault(serializeAnnotation.converter(), ValueConverter.DEFAULT);
                }
                else {
                    name = field.getName();
                    converter = ValueConverter.DEFAULT;
                }

                consumer.accept(ImmutableTriple.of(name, field, converter));
            }
        }
    }
}