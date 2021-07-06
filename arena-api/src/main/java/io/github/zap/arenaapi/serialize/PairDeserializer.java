package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class PairDeserializer extends StdDeserializer<Pair> {
    public PairDeserializer() {
        super(Pair.class);
    }

    @Override
    public Pair<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
        JsonNode jsonNode = objectMapper.readTree(p);
        var entry = jsonNode.fields().next();
        return Pair.of(entry.getKey(), entry.getValue().asText());
    }
}
