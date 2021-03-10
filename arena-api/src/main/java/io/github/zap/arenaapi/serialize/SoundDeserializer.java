package io.github.zap.arenaapi.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.io.IOException;

public class SoundDeserializer extends StdDeserializer<Sound> {
    public SoundDeserializer() {
        super(Sound.class);
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Sound deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String sound = node.get("sound").textValue();
        Sound.Source source = Sound.Source.valueOf(node.get("source").textValue());
        float volume = node.get("volume").numberValue().floatValue();
        float pitch = node.get("pitch").numberValue().floatValue();

        return Sound.sound(Key.key(sound), source, volume, pitch);
    }
}
