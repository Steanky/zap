package io.github.zap.zombies.game.data.map.shop.tmtask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeamMachineTaskDeserializer extends JsonDeserializer<TeamMachineTask> {

    @Getter
    private final Map<String, Class<? extends TeamMachineTask>> teamMachineTaskMappings = new HashMap<>();

    @Override
    public TeamMachineTask deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = objectMapper.readTree(jsonParser);

        if (jsonNode.has("type")) {
            return objectMapper.treeToValue(jsonNode, teamMachineTaskMappings.get(jsonNode.get("type").asText()));
        }

        return null;
    }
}
