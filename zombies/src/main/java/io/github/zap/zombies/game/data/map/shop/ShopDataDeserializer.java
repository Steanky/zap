package io.github.zap.zombies.game.data.map.shop;

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
 * Deserializer for shop data based on a type field
 */
public class ShopDataDeserializer extends JsonDeserializer<ShopData> {

    @Getter
    private final Map<String, Class<? extends ShopData>> shopDataClassMappings = new HashMap<>();

    @Override
    public ShopData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = objectMapper.readTree(jsonParser);

        if (jsonNode.has("type")) {
            return objectMapper.treeToValue(jsonNode, shopDataClassMappings.get(jsonNode.get("type").asText()));
        }

        return null;
    }
}
