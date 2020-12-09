package io.github.zap.arenaapi.serialize;

import io.github.zap.arenaapi.ArenaApi;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Classes that need to be serialized and deserialized should inherit from this.
 */
public abstract class DataSerializable implements ConfigurationSerializable {
    private static final Map<ClassEntry, ValueConverter<?,?>> globalConverters = new HashMap<>();
    private static final Map<String, ValueConverter<?,?>> namedConverters = new HashMap<>();
    private static final Map<String, Class<? extends DataSerializable>> classes = new HashMap<>();

    /**
     * Registers a global converter for the specified kind of object. This converter will apply to all instances of the
     * object, even as part of an array, generic collection, or map.
     * @param serializedClass The class of the object that will be serialized
     * @param deserializedClass The class of the object that will be deserialized
     * @param converter The converter that will be used to convert the object
     */
    public static <T,V> void registerGlobalConverter(Class<T> serializedClass, Class<V> deserializedClass,
                                               ValueConverter<T,V> converter) {
        Validate.notNull(serializedClass, "serializedClass cannot be null");
        Validate.notNull(deserializedClass, "deserializedClass cannot be null");
        Validate.isTrue(!serializedClass.equals(deserializedClass), "serializedClass and deserializedClass " +
                "cannot be equal to each other");

        globalConverters.putIfAbsent(new ClassEntry(serializedClass.getName(), true), converter);
        globalConverters.putIfAbsent(new ClassEntry(serializedClass.getName(), false), converter);
    }

    /**
     * Registers a local (field-specific) converter. This object is retrieved from the internal map when a field is
     * annotated with Serialize and specifies a converter name.
     * @param name The name of the converter, so it can be referenced via an annotation
     * @param converter The converter to register
     */
    public static void registerNamedConverter(String name, ValueConverter<?,?> converter) {
        Validate.notNull(name, "name cannot be null");
        Validate.notNull(converter, "converter cannot be null");
        Validate.isTrue(!name.equals(StringUtils.EMPTY), "name cannot be an empty string");
        Validate.isTrue(namedConverters.putIfAbsent(name, converter) == null, "a converter with that" +
                " name already exists");
    }

    public static void registerClass(String name, Class<? extends DataSerializable> clazz) {
        Validate.notNull(name, "name cannot be null");
        Validate.notNull(clazz, "clazz cannot be null");
        Validate.isTrue(classes.putIfAbsent(name, clazz) == null, String.format("a class with name/alias " +
                "'%s' already exists", name));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serializedData = new LinkedHashMap<>();

        TypeAlias type = getClass().getAnnotation(TypeAlias.class);
        if(type != null) {
            serializedData.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, type.alias());
        }

        forEachSerializable(getClass(), (entry) -> { //iterate through all serializable fields in this DataSerializable
            String name = entry.getName();
            Field field = entry.getField();
            @SuppressWarnings("rawtypes") ValueConverter converter = entry.getConverter();

            try {
                Object fieldValue = entry.getField().get(this);
                Object transformedValue;

                if(entry.isCollection()) { //perform more complicated recursive conversion process
                    //noinspection unchecked
                    transformedValue = (converter == null) ? processCollection(fieldValue, field.getGenericType(),
                            true) : converter.serialize(fieldValue);
                }
                else { //simple, faster conversion process for non-collection objects
                    //noinspection unchecked
                    transformedValue = (converter == null) ? processObject(fieldValue, field.getType(), true) :
                            converter.serialize(fieldValue);
                }

                serializedData.put(name, transformedValue);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException |
                    ClassNotFoundException | ClassCastException e) {
                ArenaApi.getInstance().getLogger().warning(String.format("Exception when attempting to serialize " +
                                "field '%s' in object '%s': %s", field.toGenericString(), this.toString(),
                        e.getMessage()));
            }
        });

        return serializedData;
    }

    @SuppressWarnings("unused")
    public static DataSerializable deserialize(Map<String, Object> data) {
        // get the classname from the type key, which will be the class we want to construct or an alias
        String name = (String) data.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);

        if (name != null) {
            try {
                return createDeserialized(name, data);
            }
            catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                    InvocationTargetException e) {
                ArenaApi.getInstance().getLogger().warning(String.format("An error occured when trying to instantiate" +
                        " '%s': %s", name, e.getMessage()));
            }
        }
        else {
            ArenaApi.getInstance().getLogger().warning(String.format("The serialized data does not contain required " +
                    "type key '%s'", ConfigurationSerialization.SERIALIZED_TYPE_KEY));
        }

        return null;
    }

    /**
     * Creates an actual deserialized object
     * @param className The name of the class to deserialize
     * @param data The data to deserialize with
     * @return The deserialized data
     * @throws NoSuchMethodException General deserialization error
     * @throws IllegalAccessException General deserialization error
     * @throws InvocationTargetException General deserialization error
     * @throws InstantiationException General deserialization error
     */
    private static DataSerializable createDeserialized(String className, Map<String, Object> data)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        // get the class from the classname or alias
        Class<? extends DataSerializable> instanceClass = classes.get(className);

        if(instanceClass != null) {
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

            if(!constructor.isAccessible()) {
                constructor.setAccessible(true); //required parameterless constructor can be private
            }

            Object instanceObject = constructor.newInstance(); //instantiate the object
            forEachSerializable(instanceClass, (entry) -> {
                String name = entry.getName();
                Field field = entry.getField();
                @SuppressWarnings("rawtypes") ValueConverter converter = entry.getConverter();

                Object fieldValue = data.get(name);

                try {
                    Object transformedValue;
                    if (entry.isCollection()) {
                        //noinspection unchecked
                        transformedValue = (converter == null) ? processCollection(fieldValue, field.getGenericType(),
                                false) : converter.deserialize(fieldValue);
                    } else {
                        //noinspection unchecked
                        transformedValue = (converter == null) ? processObject(fieldValue, field.getType(),
                                false) : converter.deserialize(fieldValue);
                    }

                    field.set(instanceObject, transformedValue);
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException |
                        ClassNotFoundException | ClassCastException e) {
                    ArenaApi.getInstance().getLogger().warning(String.format("Exception when attempting to " +
                                    "assign value '%s' to field '%s' in object '%s': '%s'", fieldValue.toString(),
                            field.toGenericString(), instanceObject.toString(), e.getMessage()));
                }
            });

            return (DataSerializable) instanceObject;
        }
        else {
            ArenaApi.getInstance().getLogger().warning(String.format("Class name or alias '%s' has not been " +
                    "registered and therefore will not be deserialized.", className));
        }

        return null;
    }

    /**
     * Recursively iterates through all members in a given object, performing conversions where possible to ensure
     * that the returned object can be assigned to the field with the specified type. Automatically iterates through
     * maps, collections, and arrays, converting each object they contain regardless of how deeply they are nested.
     * @param instance The object to process
     * @param fieldType The type of the field we're trying to assign to
     * @param serializing Whether or not we are serializing
     * @return The converted object
     * @throws IllegalAccessException Generic reflection exception
     * @throws InstantiationException Generic reflection exception
     * @throws ClassNotFoundException Generic reflection exception
     * @throws SecurityException Generic reflection exception
     * @throws ClassCastException Generic reflection exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object processCollection(Object instance, Type fieldType, boolean serializing)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException, SecurityException,
            ClassCastException {
        ParameterizedType parameterizedType = null;
        Class<?> instanceClass = instance.getClass();
        Class<?> fieldClass;

        if(fieldType instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType)fieldType;
            fieldClass = (Class<?>)parameterizedType.getRawType(); //extract generic type information
        }
        else {
            fieldClass = (Class<?>)fieldType; //there is no generic type information
        }

        if(instance instanceof Collection) {
            if(fieldClass.isArray()) { //fix bukkit serializing arrays to lists
                Class<?> arrayComponent = fieldClass.getComponentType();

                Object[] array = ((Collection<?>)instance).toArray();
                Object newArray = Array.newInstance(arrayComponent, array.length);

                for(int i = 0; i < array.length; i++) {
                    Array.set(newArray, i, processCollection(array[i], arrayComponent, serializing));
                }

                return newArray;
            }
            else if(fieldClass.isAssignableFrom(instance.getClass())) { //recursively parse collections
                Collection newCollection = (Collection)instanceClass.newInstance();
                Type nextType = parameterizedType == null ? Object.class : parameterizedType.getActualTypeArguments()[0];

                for(Object element : (Collection)instance) {
                    newCollection.add(processCollection(element, nextType, serializing));
                }

                return newCollection;
            }
        }
        else if(instance instanceof Map) { //also recursively parse maps
            Map oldMap = (Map) instance;
            Map newMap = (Map) instanceClass.newInstance();

            Type nextKeyType;
            Type nextValueType;
            if (parameterizedType == null) {
                nextKeyType = Object.class;
                nextValueType = Object.class;
            } else {
                nextKeyType = parameterizedType.getActualTypeArguments()[0];
                nextValueType = parameterizedType.getActualTypeArguments()[1];
            }

            //noinspection Convert2Lambda
            oldMap.forEach(new BiConsumer<Object, Object>() {
                @SuppressWarnings("unchecked")
                @SneakyThrows
                @Override
                public void accept(Object key, Object value) {
                    newMap.put(processCollection(key, nextKeyType, serializing), processCollection(value, nextValueType,
                            serializing));
                }
            });

            return newMap;
        }

        return processObject(instance, fieldClass, serializing);
    }

    /**
     * Processes an individual object that may or may not be a collection. Does not perform a deep conversion or any
     * iteration over contained objects. Otherwise, works the same way as processCollection.
     * @param instance The object to process
     * @param fieldClass The class of the field we are converting to
     * @param serializing Whether or not we are serializing
     * @return The converted object
     * @throws ClassNotFoundException Generic reflection exception
     * @throws ClassCastException Generic reflection exception
     */
    private static Object processObject(Object instance, Class<?> fieldClass, boolean serializing)
            throws ClassNotFoundException, ClassCastException {
        Class<?> instanceClass = instance.getClass();

        if(serializing && instance instanceof Enum) { //implement baked in support for all enums via EnumWrapper
            Enum<?> enumInstance = (Enum<?>)instance;
            return new EnumWrapper(enumInstance.getClass().getName(), enumInstance.name());
        }
        else if(!serializing && instance instanceof EnumWrapper) {
            EnumWrapper wrapper = (EnumWrapper)instance;
            //noinspection unchecked,rawtypes,rawtypes
            return Enum.valueOf((Class)Class.forName(wrapper.getEnumClass()), wrapper.getEnumValue());
        }

        /*
        check for global object converter if we're serializing, or deserializing and the object types are
        not compatible.
         */
        if(serializing || !fieldClass.isAssignableFrom(instanceClass)) {
            String instanceName = instanceClass.getName();
            @SuppressWarnings("rawtypes") ValueConverter converter = globalConverters.get(new ClassEntry(instanceName,
                    serializing));

            if(converter != null) {
                if(serializing) {
                    //noinspection unchecked
                    return converter.serialize(instance);
                }
                else {
                    //noinspection unchecked
                    return converter.deserialize(instance);
                }
            }
        }

        return instance;
    }

    /**
     * Runs method on every field of a class that should be serializable
     * @param clazz The class to run the method on
     * @param consumer A Consumer that takes an ImmutableTriple containing the name of the value (which may be either
     *                 the field name or the name parameter of the @Serialize annotation), the field that we are dealing
     *                 with, and a ValueConverter that will default to a converter which does nothing.
     */
    private static void forEachSerializable(Class<?> clazz, Consumer<SerializationEntry> consumer) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) { // skip static fields
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Serialize serializeAnnotation = field.getAnnotation(Serialize.class);
                String name;
                ValueConverter<?,?> converter;
                boolean isAggregation;
                if(serializeAnnotation != null) {
                    if(serializeAnnotation.skip()) {
                        continue;
                    }

                    String serializeName = serializeAnnotation.name();

                    name = serializeName.equals(StringUtils.EMPTY) ? field.getName() : serializeName;
                    converter = namedConverters.get(serializeAnnotation.converter());
                    isAggregation = serializeAnnotation.isAggregation();
                }
                else {
                    name = field.getName();
                    converter = null;
                    isAggregation = false;
                }

                consumer.accept(new SerializationEntry(name, field, converter, isAggregation));
            }
        }
    }
}