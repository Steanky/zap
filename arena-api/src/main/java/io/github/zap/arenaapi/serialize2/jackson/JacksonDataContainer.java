package io.github.zap.arenaapi.serialize2.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zap.arenaapi.serialize2.DataContainer;
import io.github.zap.arenaapi.serialize2.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord") //bad unintelliJ
class JacksonDataContainer implements DataContainer {
    private static class TypeReferenceWrapper<T> extends TypeReference<T> {
        private final Type type;

        private TypeReferenceWrapper(TypeToken<?> token) {
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

    private <T> Optional<T> getObjectInternal(TypeToken<T> type, String... keys) {
        JsonNode current = this.node;
        for(String key : keys) {
            current = current.get(key);

            if(current == null || current.isNull()) {
                return Optional.empty();
            }
        }

        return Optional.of(mapper.convertValue(current, new TypeReferenceWrapper<>(type)));
    }

    private <T> Optional<T> getObjectInternal(Class<T> type, String... keys) {
        return getObjectInternal(new TypeToken<>(type) {}, keys);
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull Class<T> type, @NotNull String... keys) {
        return getObjectInternal(type, keys);
    }

    @Override
    public @NotNull <T> Optional<T> getObject(@NotNull TypeToken<T> typeToken, @NotNull String... keys) {
        return getObjectInternal(typeToken, keys);
    }

    @Override
    public @NotNull Optional<String> getString(@NotNull String... keys) {
        return getObjectInternal(String.class, keys);
    }

    @Override
    public @NotNull Optional<Boolean> getBoolean(@NotNull String... keys) {
        return getObjectInternal(Boolean.class, keys);
    }

    @Override
    public @NotNull Optional<Byte> getByte(@NotNull String... keys) {
        return getObjectInternal(Byte.class, keys);
    }

    @Override
    public @NotNull Optional<Short> getShort(@NotNull String... keys) {
        return getObjectInternal(Short.class, keys);
    }

    @Override
    public @NotNull Optional<Integer> getInt(@NotNull String... keys) {
        return getObjectInternal(Integer.class, keys);
    }

    @Override
    public @NotNull Optional<Character> getChar(@NotNull String... keys) {
        return getObjectInternal(Character.class, keys);
    }

    @Override
    public @NotNull Optional<Long> getLong(@NotNull String... keys) {
        return getObjectInternal(Long.class, keys);
    }

    @Override
    public @NotNull Optional<Float> getFloat(@NotNull String... keys) {
        return getObjectInternal(Float.class, keys);
    }

    @Override
    public @NotNull Optional<Double> getDouble(@NotNull String... keys) {
        return getObjectInternal(Double.class, keys);
    }

    /**
     * Gets the underlying JsonNode for this JacksonDataContainer instance
     * @return The underlying JsonNode
     */
    public @NotNull JsonNode node() {
        return node;
    }

    /**
     * Gets the ObjectMapper associated with this JacksonDataContainer
     * @return The associated ObjectMapper
     */
    public @NotNull ObjectMapper mapper() {
        return mapper;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JacksonDataContainer container) {
            return node.equals(container.node);
        }

        return false;
    }

    @Override
    public String toString() {
        return "JacksonDataContainer{node=" + node + "}";
    }
}