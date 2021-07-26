package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class PairDeserializer extends StdDeserializer<Pair> {
    public PairDeserializer() {
        super(Pair.class);
    }

    @Override
    public Pair<String, String> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = parser.getCodec().readTree(parser);
        String left = jsonNode.get("left").asText();
        String right = jsonNode.get("right").asText();

        return Pair.of(left, right);
    }
}
