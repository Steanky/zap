package io.github.zap.arenaapi.serialize2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord") //bad unintelliJ
class JacksonDataContainer implements DataContainer {
    private static abstract class TypeReferenceWrapper<T> extends TypeReference<T> {
        private final Type type;

        TypeReferenceWrapper(Class<?> clazz) {
            super();
            type = clazz;
        }

        TypeReferenceWrapper(TypeToken<?> token) {
            super();
            type = token.type();
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    private final ObjectMapper mapper;
    private final JsonNode node;

    JacksonDataContainer(@NotNull ObjectMapper mapper, @NotNull JsonNode node) {
        this.mapper = mapper;
        this.node = node;
    }

    private <T> Optional<T> getObjectInternal(TypeReference<T> type, String... keys) {
        JsonNode current = this.node;
        for(String key : keys) {
            current = current.get(key);

            if(current == null || current.isNull()) {
                return Optional.empty();
            }
        }

        return Optional.of(mapper.convertValue(current, type));
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(type) {}, keys);
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull TypeToken<T> typeToken, @NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(typeToken) {}, keys);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(String.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Boolean.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Byte> getByte(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Byte.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Short> getShort(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Short.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Integer.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Character> getChar(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Character.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Long.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Float.class) {}, keys);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull String... keys) {
        return getObjectInternal(new TypeReferenceWrapper<>(Double.class) {}, keys);
    }

    public JsonNode node() {
        return node;
    }
}