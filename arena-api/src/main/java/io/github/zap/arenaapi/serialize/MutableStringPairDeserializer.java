package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;

public class MutableStringPairDeserializer extends StdDeserializer<MutablePair<String, String>> {
    public MutableStringPairDeserializer() {
        super(MutablePair.class);
    }

    @Override
    public MutablePair<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
        JsonNode jsonNode = objectMapper.readTree(p);
        var entry = jsonNode.fields().next();
        return MutablePair.of(entry.getKey(), entry.getValue().asText());
    }
}
