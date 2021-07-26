package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.kyori.adventure.sound.Sound;

import java.io.IOException;

public class SoundSerializer extends StdSerializer<Sound> {
    public SoundSerializer() {
        super(Sound.class);
    }

    @Override
    public void serialize(Sound value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("sound", value.name().asString());
        gen.writeStringField("source", value.source().toString());
        gen.writeNumberField("volume", value.volume());
        gen.writeNumberField("pitch", value.pitch());
        gen.writeEndObject();
    }
}