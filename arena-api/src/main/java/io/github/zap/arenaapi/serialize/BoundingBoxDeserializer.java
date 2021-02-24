package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.IOException;

public class BoundingBoxDeserializer extends StdDeserializer<BoundingBox> {

    public BoundingBoxDeserializer() {
        super(BoundingBox.class);
    }

    @Override
    public BoundingBox deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        JsonNode corner1 = node.get("corner1");
        JsonNode corner2 = node.get("corner2");

        Vector first = new Vector(corner1.get("x").asDouble(), corner1.get("y").asDouble(), corner1.get("z").asDouble());
        Vector second = new Vector(corner2.get("x").asDouble(), corner2.get("y").asDouble(), corner2.get("z").asDouble());

        return BoundingBox.of(first, second);
    }
}
