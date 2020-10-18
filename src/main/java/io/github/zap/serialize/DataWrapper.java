package io.github.zap.serialize;

import io.github.zap.ZombiesPlugin;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DataWrapper<T extends DataSerializable> {
    @Getter
    private final SerializationProvider serializationProvider;

    @Getter
    private final T data;

    public DataWrapper(SerializationProvider serializationProvider, T data) {
        this.serializationProvider = serializationProvider;
        this.data = data;
    }

    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();
        Field[] fields = data.getClass().getDeclaredFields();

        for(Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers())) {
                if(!field.isAccessible()) {
                    field.setAccessible(true); //serialize private members
                }

                Annotation[] annotations = field.getDeclaredAnnotations();

                for(Annotation annotation : annotations) {
                    if(annotation.annotationType() == Serialize.class) {
                        Serialize serializeAnnotation = (Serialize)annotation;
                        String annotationName = serializeAnnotation.name();

                        try {
                            Object fieldValue = field.get(data);
                            serializedData.put(annotationName, serializationProvider.wrap(fieldValue));
                        } catch (IllegalAccessException ignored) {
                            ZombiesPlugin.getInstance().getLogger().warning(String.format("IllegalAccess exception when " +
                                    "attempting to serialize field '%s' in object '%s'", field.toGenericString(), data.toString()));
                        }
                        break;
                    }
                }
            }
        }

        return serializedData;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DataWrapper) {
            return ((DataWrapper<?>) other).data.equals(data);
        }

        return false;
    }
}
