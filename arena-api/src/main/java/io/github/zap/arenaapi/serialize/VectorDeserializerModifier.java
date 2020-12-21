package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.util.Vector;

import java.io.IOException;

public class VectorDeserializerModifier extends StdDeserializer<Vector> {
    public VectorDeserializerModifier() {
        this(null);
    }

    protected VectorDeserializerModifier(Class<Vector> t) {
        super(t);
    }

    @Override
    public Vector deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);
        double x = (Integer)(node.get("x")).numberValue();
        double y = (Integer)(node.get("y")).numberValue();
        double z = (Integer)(node.get("z")).numberValue();

        return new Vector(x, y, z);
    }
}
