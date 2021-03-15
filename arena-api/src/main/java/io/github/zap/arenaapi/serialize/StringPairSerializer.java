package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class StringPairSerializer extends JsonDeserializer<Pair<String, String>> {

    @Override
    public Pair<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
        JsonNode jsonNode = objectMapper.readTree(p);
        var entry = jsonNode.fields().next();
        return Pair.of(entry.getKey(), entry.getValue().asText());
    }

}
