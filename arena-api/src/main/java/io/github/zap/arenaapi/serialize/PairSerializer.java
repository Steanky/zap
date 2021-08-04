package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;

public final class PairSerializer extends JsonSerializer<Pair> {
    @Override
    public void serialize(Pair value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("left", value.getLeft());
        gen.writeObjectField("right", value.getRight());
        gen.writeEndObject();
    }
}
