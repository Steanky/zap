package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord") //no it can't, map field should not be public smh my head
class StandardDataContainer implements DataContainer {
    private final Map<String, Object> map;
    private final ConverterRegistry converters;

    StandardDataContainer(@NotNull Map<String, Object> map, @NotNull ConverterRegistry converters) {
        this.map = map;
        this.converters = converters;
    }

    private <T> Optional<T> getObjectPathInternal(TypeInformation info, DataKey ... keys) {
        if(keys.length == 0) {
            throw new IllegalArgumentException("getObject called without providing any DataKeys");
        }

        StandardDataContainer lastContainer = this;
        for(int i = 0; i < keys.length - 1; i++) {
            DataKey key = keys[i];
            Optional<StandardDataContainer> object = getObjectInternal(lastContainer,
                    new TypeInformation(StandardDataContainer.class), key);

            if(object.isEmpty()) {
                return Optional.empty();
            }
            else {
                lastContainer = object.get();
            }
        }

        return getObjectInternal(lastContainer, info, keys[keys.length - 1]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Optional<T> getObjectInternal(StandardDataContainer container, TypeInformation info, DataKey key) {
        Object data = container.map.get(key.key());
        Class<?> classType = info.type();

        if(data == null) { //missing value
            return Optional.empty();
        }
        else if(classType.isAssignableFrom(data.getClass())) { //easiest case, we can just cast
            return Optional.of((T)data);
        }
        else { //type mismatch, try to perform conversion
            Converter converter = converters.deserializerFor(data.getClass(), classType);

            if(converter != null) {
                Object converted = converter.convert(data, info);

                if(converted != null && classType.isAssignableFrom(converted.getClass())) { //conversion gave us what we needed
                    return Optional.of((T)converted);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(type), keys[keys.length - 1]);
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull TypeInformation typeInformation, @NotNull DataKey... keys) {
        return getObjectPathInternal(typeInformation, keys[keys.length - 1]);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(String.class), keys);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Boolean.class), keys);
    }

    @Override
    public @NotNull Optional<Byte> getByte(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Byte.class), keys);
    }

    @Override
    public @NotNull Optional<Short> getShort(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Short.class), keys);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Integer.class), keys);
    }

    @Override
    public @NotNull Optional<Character> getChar(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Character.class), keys);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Long.class), keys);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Float.class), keys);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull DataKey... keys) {
        return getObjectPathInternal(new TypeInformation(Double.class), keys);
    }

    @Override
    public @NotNull Map<String, Object> objectMapping() {
        return new LinkedHashMap<>(map);
    }
}