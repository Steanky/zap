package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Color;

import java.io.IOException;

public class ColorDeserializer extends StdDeserializer<Color> {

    public ColorDeserializer() {
        super(Color.class);
    }

    @Override
    public Color deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        node = node.get("color");
        int r = node.get(0).asInt();
        int g = node.get(1).asInt();
        int b = node.get(2).asInt();

        return Color.fromRGB(r, g, b);
    }
}
