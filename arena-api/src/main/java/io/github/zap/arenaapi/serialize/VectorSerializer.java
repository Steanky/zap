package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.util.Vector;

import java.io.IOException;

public class VectorSerializer extends StdSerializer<Vector> {
    public VectorSerializer() {
        super(Vector.class);
    }

    @Override
    public void serialize(Vector value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", value.getX());
        gen.writeNumberField("y", value.getY());
        gen.writeNumberField("z", value.getZ());
        gen.writeEndObject();
    }
}
