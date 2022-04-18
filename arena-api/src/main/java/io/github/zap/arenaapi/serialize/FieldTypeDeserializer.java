package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializes an abstract class based on a field with mappings of possible field names to their respective classes
 * @param <T> The abstract type to deserialize
 */
@RequiredArgsConstructor
public class FieldTypeDeserializer<T> extends JsonDeserializer<T> {

    @Getter
    private final Map<String, Class<? extends T>> mappings = new HashMap<>();

    private final String typeFieldName;

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = objectMapper.readTree(jsonParser);

        if (jsonNode.has(typeFieldName)) {
            return objectMapper.treeToValue(jsonNode, mappings.get(jsonNode.get(typeFieldName).asText()));
        }

        return null;
    }
}
