package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.util.Vector;

import java.io.IOException;

public class VectorDeserializer extends StdDeserializer<Vector> {
    public VectorDeserializer() {
        super(Vector.class);
    }

    @Override
    public Vector deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        double x = (double)(node.get("x")).numberValue();
        double y = (double)(node.get("y")).numberValue();
        double z = (double)(node.get("z")).numberValue();

        return new Vector(x, y, z);
    }
}
