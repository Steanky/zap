package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class DataSerializable implements ConfigurationSerializable {
    private static final Map<String, ValueConverter> converters = new HashMap<>();

    /**
     * Registers a ValueConverter that can be used to transform the values of fields during serialization and
     * deserialization.
     * @param name The name of the converter
     * @param converter The converter
     */
    public static void registerConverter(String name, ValueConverter converter){
        converters.put(name, converter);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for(Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers())) { //skip static fields
                Annotation[] annotations = field.getDeclaredAnnotations();

                boolean skip = false;
                Serialize serializeAnnotation = null;
                for(Annotation annotation : annotations) {
                    Class<?> annotationType = annotation.annotationType();

                    if(annotationType == NoSerialize.class) {
                        skip = true;
                        break;
                    }
                    else if(annotationType == Serialize.class) {
                        serializeAnnotation = (Serialize)annotation;
                    }
                }

                if(!skip) {
                    if(!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    String name = null;
                    ValueConverter converter = null;
                    if(serializeAnnotation != null) {
                        String annotationName = serializeAnnotation.name();
                        name = annotationName.equals(StringUtils.EMPTY) ? field.getName() : annotationName;
                        converter = converters.getOrDefault(serializeAnnotation.converter(), ValueConverter.DEFAULT);
                    }

                    try {
                        Object data = field.get(this);

                        if(serializeAnnotation != null) {
                            serializedData.put(name, converter.convert(data, Direction.SERIALIZE));
                        }
                        else {
                            serializedData.put(field.getName(), data);
                        }
                    } catch (IllegalAccessException e) {
                        ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when attempting " +
                                        "to serialize field '%s' in object '%s': %s", field.toGenericString(),
                                this.toString(), e.getMessage()));
                    }
                }
            }
        }

        return serializedData;
    }

    public static DataSerializable deserialize(Map<String, Object> data) {
        //get the classname from the type key, which will be the class we want to construct
        String className = (String)data.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);

        if(className != null) {
            try {
                //get the class from the classname
                Class<?> instanceClass = Class.forName(className);

                if(DataSerializable.class.isAssignableFrom(instanceClass)) { //check if our instance class is DataSerializable
                    Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

                    if(!constructor.isAccessible()) {
                        constructor.setAccessible(true); //required parameterless constructor can be private
                    }

                    DataSerializable instanceObject = (DataSerializable)constructor.newInstance(); //instantiate the object
                    Field[] fields = instanceClass.getDeclaredFields();

                    for(Field field : fields) {
                        if(!Modifier.isStatic(field.getModifiers())) { //do not deserialize static members
                            Annotation[] annotations = field.getDeclaredAnnotations();

                            boolean skip = false;
                            Serialize serializeAnnotation = null;
                            for(Annotation annotation : annotations) {
                                Class<?> annotationType = annotation.annotationType();

                                if(annotationType == NoSerialize.class) {
                                    skip = true;
                                    break;
                                }
                                else if(annotationType == Serialize.class) {
                                    serializeAnnotation = (Serialize)annotation;
                                }
                            }

                            if(!skip) { //skip all fields tagged with @NoSerialize
                                if(!field.isAccessible()) {
                                    field.setAccessible(true); //deserialize private members too
                                }

                                String name = null;
                                ValueConverter converter = null;
                                if(serializeAnnotation != null) { //try to get Serialize annotation values
                                    String annotationName = serializeAnnotation.name();
                                    name = annotationName.equals(StringUtils.EMPTY) ? field.getName() : annotationName;
                                    converter = converters.getOrDefault(serializeAnnotation.converter(), ValueConverter.DEFAULT);
                                }

                                try {
                                    Object rawValue = data.get(name); //get the serialized data
                                    Class<?> fieldType = field.getType();

                                    //workaround for ConfigurationSerialization giving us arraylists when we need arrays
                                    if(rawValue instanceof ArrayList && fieldType.isArray()) {
                                        rawValue = toArrayDeep((ArrayList<?>)rawValue, fieldType);
                                    }

                                    if(serializeAnnotation != null) {
                                        field.set(instanceObject, converter.convert(rawValue, Direction.DESERIALIZE));
                                    }
                                    else {
                                        field.set(instanceObject, rawValue);
                                    }
                                } catch (IllegalAccessException | IllegalArgumentException e) {
                                    ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception " +
                                                    "when attempting to serialize field '%s' in object '%s': %s",
                                            field.toGenericString(), data.toString(), e.getMessage()));
                                }
                                break;
                            }
                        }
                    }

                    return instanceObject;
                }
                else {
                    ZombiesPlugin.getInstance().getLogger().warning(String.format("'%s' is not an instance of " +
                            "DataSerializable", className));
                }
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

    //recursive utility function: deep-converts arraylists into arrays (handles any 'dimension' of arraylist)
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