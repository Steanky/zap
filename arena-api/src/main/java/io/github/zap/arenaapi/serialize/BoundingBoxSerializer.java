package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.IOException;

public class BoundingBoxSerializer extends StdSerializer<BoundingBox> {
    protected BoundingBoxSerializer() {
        super(BoundingBox.class);
    }

    @Override
    public void serialize(BoundingBox value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("corner1", new Vector(value.getMinX(), value.getMinY(), value.getMinZ()));
        gen.writeObjectField("corner2", new Vector(value.getMaxX(), value.getMaxY(), value.getMaxZ()));
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(BoundingBox value, JsonGenerator gen, SerializerProvider serializers,
                                  TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }
}
