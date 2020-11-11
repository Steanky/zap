package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import lombok.SneakyThrows;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Classes that need to be serialized and deserialized should inherit from this.
 */
public abstract class DataSerializable implements ConfigurationSerializable {
    private static final Map<Class<?>, ValueConverter> serializedGlobals = new HashMap<>();
    private static final Map<Class<?>, ValueConverter> deserializedGlobals = new HashMap<>();

    /**
     * Registers a global converter for the specified kind of object. This converter will apply to all instances of the
     * object, even as part of an array, generic collection, or map.
     * @param serializedClass The class of the object that will be serialized
     * @param deserializedClass The class of the object that will be deserialized
     * @param converter The converter that will be used to convert the object
     */
    public static void registerGlobalConverter(Class<?> serializedClass, Class<?> deserializedClass, ValueConverter converter) {
        serializedGlobals.put(serializedClass, converter);
        deserializedGlobals.put(deserializedClass, converter);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();

        forEachSerializable(getClass(), (entry) -> { //iterate through all serializable fields in this DataSerializable
            String name = entry.getName();
            Field field = entry.getField();
            ValueConverter converter = entry.getConverter();

            try {
                Object fieldValue = entry.getField().get(this);
                Object transformedValue;

                if(entry.isCollection()) { //perform more complicated recursive conversion process
                    transformedValue = (converter == null) ? processCollection(fieldValue, field.getGenericType(),
                            true) : converter.convert(fieldValue, true);
                }
                else { //simple conversion process for simple, non-collection objects
                    transformedValue = (converter == null) ? processObject(fieldValue, field.getType(), true) :
                            converter.convert(fieldValue, true);
                }

                serializedData.put(name, transformedValue);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException |
                    ClassNotFoundException e) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting " +
                                "to serialize field '%s' in object '%s': %s", field.toGenericString(), this.toString(),
                        e.getMessage()));
            }
        });

        return serializedData;
    }

    @SuppressWarnings("unused")
    public static DataSerializable deserialize(Map<String, Object> data) {
        // get the classname from the type key, which will be the class we want to construct
        String className = (String) data.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);

        if (className != null) {
            try {
                return createDeserialized(className, data);
            }
            catch (ClassNotFoundException ignored) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' does not exist", className));
            }
            catch(IllegalStateException ignored) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' does not extend DataSerializable.",
                        className));
            }
            catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
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
            InstantiationException, IllegalStateException {
        // get the class from the classname
        Class<?> instanceClass = Class.forName(className);

        if(DataSerializable.class.isAssignableFrom(instanceClass)) { //check if our instance class is DataSerializable
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

            if(!constructor.isAccessible()) {
                constructor.setAccessible(true); //required parameterless constructor can be private
            }

            Object instanceObject = constructor.newInstance(); //instantiate the object

            forEachSerializable(instanceClass, (entry) -> {
                String name = entry.getName();
                Field field = entry.getField();
                ValueConverter converter = entry.getConverter();

                Object fieldValue = data.get(name);

                try {
                    Object transformedValue;
                    if(entry.isCollection()) {
                        transformedValue = (converter == null) ? processCollection(fieldValue,
                                field.getGenericType(), false) : converter.convert(fieldValue, false);
                    }
                    else {
                        transformedValue = (converter == null) ? processObject(fieldValue, field.getType(),
                                false) : converter.convert(fieldValue, false);
                    }

                    field.set(instanceObject, transformedValue);
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException |
                        ClassNotFoundException e) {
                    ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting to " +
                            "assign value '%s' to field '%s' in object '%s': '%s'", fieldValue.toString(),
                            field.toGenericString(), instanceObject.toString(), e.getMessage()));
                }
            });

            return (DataSerializable) instanceObject;
        }
        else {
            throw new IllegalStateException(String.format("'%s' is not an instance of DataSerializable", className));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object processCollection(Object instance, Type fieldType, boolean serializing)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
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
                    newMap.put(processCollection(key, nextKeyType, serializing),
                            processCollection(value, nextValueType, serializing));
                }
            });

            return newMap;
        }

        return processObject(instance, fieldClass, serializing);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object processObject(Object instance, Class<?> fieldClass, boolean serializing)
            throws ClassNotFoundException {
        Class<?> instanceClass = instance.getClass();

        if(serializing && instance instanceof Enum) { //implement baked in support for all enums via EnumWrapper
            Enum<?> enumInstance = (Enum<?>)instance;
            return new EnumWrapper(enumInstance.getClass().getName(), enumInstance.name());
        }
        else if(!serializing && instance instanceof EnumWrapper) {
            EnumWrapper wrapper = (EnumWrapper)instance;
            return Enum.valueOf((Class)Class.forName(wrapper.getEnumClass()), wrapper.getEnumValue());
        }

        /*
        check for global object converter if we're serializing, or deserializing and the object types are
        not compatible.
         */
        if(serializing || !fieldClass.isAssignableFrom(instanceClass)) {
            ValueConverter converter = (serializing) ? serializedGlobals.get(instanceClass) :
                    deserializedGlobals.get(instanceClass);

            if(converter != null) {
                return converter.convert(instance, serializing);
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

        fields:
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) { // skip static fields
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Annotation[] annotations = field.getDeclaredAnnotations();
                Serialize serializeAnnotation = null;
                boolean serializeCollection = false;

                for (Annotation annotation : annotations) { //look for Serialize annotation
                    if(annotation instanceof Serialize) {
                        serializeAnnotation = (Serialize)annotation; //keep checking annotations
                    }
                    else if(annotation instanceof SerializeCollection) {
                        serializeCollection = true;
                    }
                    else if(annotation instanceof NoSerialize) {
                        continue fields; //one NoSerialize will override a Serialize annotation
                    }
                }

                String name;
                ValueConverter converter;
                if(serializeAnnotation != null) {
                    name = serializeAnnotation.name();
                    converter = serializeAnnotation.converter().getValueConverter();
                }
                else {
                    name = field.getName();
                    converter = null;
                }

                consumer.accept(new SerializationEntry(name, field, converter, serializeCollection));
            }
        }
    }
}