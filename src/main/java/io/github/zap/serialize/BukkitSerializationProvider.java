package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class BukkitSerializationProvider implements SerializationProvider {
    @Override
    public void save(DataSerializable data, String path, String name) {
        FileConfiguration configuration = new YamlConfiguration();
        configuration.set(name, new BukkitDataWrapper<>(this, data));

        try {
            configuration.save(path);
        } catch (IOException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("IOException when attempting to save to %s", path));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializable> T load(String path, String name) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(path));
        BukkitDataWrapper<T> wrapper = (BukkitDataWrapper<T>)configuration.get(name);

        if(wrapper != null) {
            return wrapper.getData();
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Unable to load data from the config file %s:" +
                    " no data under the specified name '%s' exists", path, name));
        }

        return null;
    }

    @Override
    public <T extends DataSerializable> DataWrapper<T> deserialize(Map<String, Object> data) {
        String className = (String)data.get("typeClass");

        if(className != null) {
            try {
                Class<?> instanceClass = Class.forName(className);

                if(DataSerializable.class.isAssignableFrom(instanceClass)) { //check if our instance class is DataSerializable
                    Constructor<?> constructor = instanceClass.getDeclaredConstructor(); //get the parameterless constructor

                    if(!constructor.isAccessible()) {
                        constructor.setAccessible(true); //required parameterless constructor can be private
                    }

                    Object instanceObject = constructor.newInstance();; //instantiate the object
                    Field[] fields = instanceClass.getDeclaredFields();

                    for(Field field : fields) {
                        if(!Modifier.isStatic(field.getModifiers())) { //do not deserialize static members
                            if(!field.isAccessible()) {
                                field.setAccessible(true); //deserialize private members too
                            }

                            Annotation[] annotations = field.getDeclaredAnnotations();

                            for(Annotation annotation : annotations) {
                                if(annotation.annotationType() == Serialize.class) { //only process @Serialize
                                    Serialize serializeAnnotation = (Serialize)annotation;
                                    String annotationName = serializeAnnotation.name();

                                    try {
                                        Object rawValue = data.get(annotationName); //get the serialized data

                                        if(rawValue != null && rawValue.getClass() == ArrayList.class) {
                                            //when this value is non-null, the field is an array
                                            Class<?> componentType = field.getType().getComponentType();

                                            //workaround for ConfigurationSerialization giving us arraylists when we should have arrays
                                            if(componentType != null) {
                                                Object temp = unwrapArray(((ArrayList<?>)rawValue).toArray());

                                                int length = Array.getLength(temp);
                                                rawValue = Array.newInstance(componentType, length);

                                                //noinspection SuspiciousSystemArraycopy
                                                System.arraycopy(temp, 0, rawValue, 0, length);
                                                field.set(instanceObject, rawValue);
                                                break;
                                            }
                                        }

                                        field.set(instanceObject, unwrap(rawValue));
                                    } catch (IllegalAccessException | IllegalArgumentException e) {
                                        ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when " +
                                                        "attempting to serialize field '%s' in object '%s': %s",
                                                field.toGenericString(), data.toString(), e.getMessage()));
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    //noinspection unchecked
                    return new BukkitDataWrapper<>(this, (T)instanceObject);
                }
                else {
                    ZombiesPlugin.getInstance().getLogger().warning(String.format("Object of type %s is not an instance of " +
                            "DataSerializable", className));
                }
            } catch (ClassNotFoundException ignored) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("Unable to deserialize object data: " +
                        "the specified class %s cannot be found", className));
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                ZombiesPlugin.getInstance().getLogger().warning(String.format("An error occured when trying to instantiate " +
                        "serializable class %s: %s", className, e.getMessage()));
            }
        }
        else {
            ZombiesPlugin.getInstance().getLogger().warning("The serialized data does not contain required key " +
                    "typeClass");
        }

        return null;
    }

    @Override
    public Object wrap(Object data) {
        if(data instanceof DataSerializable) {
            return new BukkitDataWrapper<>(this, (DataSerializable)data);
        }
        else if(data instanceof Object[]) {
            return wrapArray(data);
        }

        return data;
    }

    @Override
    public Object unwrap(Object data) {
        if(data instanceof BukkitDataWrapper) {
            return ((BukkitDataWrapper<?>)data).getData();
        }
        else if(data instanceof Object[]) {
            return unwrapArray(data);
        }

        return data;
    }

    private Object wrapArray(Object array) {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        Object wrapped;

        if(DataSerializable.class.isAssignableFrom(componentType)) {
            wrapped = new BukkitDataWrapper<?>[length];
        }
        else {
            wrapped = Array.newInstance(componentType, length);
        }

        for(int i = 0; i < length; i++) {
            Array.set(wrapped, i, wrap(Array.get(array, i)));
        }

        return wrapped;
    }

    private Object unwrapArray(Object array) {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();
        Object unwrapped;

        if(BukkitDataWrapper.class.isAssignableFrom(componentType)) {
            unwrapped = new DataSerializable[length];
        }
        else {
            unwrapped = Array.newInstance(componentType, length);
        }

        for(int i = 0; i < length; i++) {
            Array.set(unwrapped, i, unwrap(Array.get(array, i)));
        }

        return unwrapped;
    }
}
