package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class DataSerializable implements ConfigurationSerializable {
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for(Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers())) { //skip static fields
                Annotation[] annotations = field.getDeclaredAnnotations();

                for(Annotation annotation : annotations) {
                    if(annotation.annotationType() == Serialize.class) {
                        if(!field.isAccessible()) {
                            field.setAccessible(true); //serialize private members too
                        }

                        Serialize serializeAnnotation = (Serialize)annotation;
                        String annotationName = serializeAnnotation.name();

                        try {
                            serializedData.put(annotationName, field.get(this));
                        } catch (IllegalAccessException ignored) {
                            ZombiesPlugin.getInstance().getLogger().warning(String.format("IllegalAccess exception " +
                                    "when attempting to serialize field '%s' in object '%s'", field.toGenericString(),
                                    this.toString()));
                        }
                        break;
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

                    Object instanceObject = constructor.newInstance(); //instantiate the object
                    Field[] fields = instanceClass.getDeclaredFields();

                    for(Field field : fields) {
                        if(!Modifier.isStatic(field.getModifiers())) { //do not deserialize static members
                            Annotation[] annotations = field.getDeclaredAnnotations();

                            for(Annotation annotation : annotations) {
                                if(annotation.annotationType() == Serialize.class) { //only process @Serialize
                                    if(!field.isAccessible()) {
                                        field.setAccessible(true); //deserialize private members too
                                    }

                                    Serialize serializeAnnotation = (Serialize)annotation;
                                    String annotationName = serializeAnnotation.name();

                                    try {
                                        Object rawValue = data.get(annotationName); //get the serialized data
                                        Class<?> fieldType = field.getType();

                                        //try to modify the value that we get from ConfigurationSerialization
                                        if(rawValue instanceof ArrayList && fieldType.isArray()) {
                                            //workaround for ConfigurationSerialization giving us arraylists when we need arrays
                                            rawValue = toArrayDeep((ArrayList<?>)rawValue, fieldType);

                                            //TODO: make it so that lists of arrays deserialize into lists of arrays instead of lists of lists
                                        }

                                        field.set(instanceObject, rawValue);
                                    } catch (IllegalAccessException | IllegalArgumentException e) {
                                        ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception " +
                                                        "when attempting to serialize field '%s' in object '%s': %s",
                                                field.toGenericString(), data.toString(), e.getMessage()));
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    return (DataSerializable)instanceObject;
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