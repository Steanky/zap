package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.io.IOException;

public class DustOptionsDeserializer extends StdDeserializer<Particle.DustOptions> {

    public DustOptionsDeserializer() {
        super(Particle.DustOptions.class);
    }

    @Override
    public Particle.DustOptions deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();
        JsonNode node = objectMapper.readTree(parser);

        Color color = objectMapper.treeToValue(node, Color.class);
        double size = node.get("size").asDouble();

        return new Particle.DustOptions(color, (float) size);
    }
}
