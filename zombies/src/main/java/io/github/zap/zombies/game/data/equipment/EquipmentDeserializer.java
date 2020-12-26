package io.github.zap.zombies.game.data.equipment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializer for equipment data based on a type field
 */
public class EquipmentDeserializer extends JsonDeserializer<EquipmentData<?>> {

    @Getter
    private final Map<String, Class<? extends EquipmentData<?>>> equipmentClassMappings = new HashMap<>();

    @Override
    public EquipmentData<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = objectMapper.readTree(jsonParser);

        if (jsonNode.has("type")) {
            return objectMapper.treeToValue(jsonNode, equipmentClassMappings.get(jsonNode.get("type").asText()));
        }
        return null;
    }
}
