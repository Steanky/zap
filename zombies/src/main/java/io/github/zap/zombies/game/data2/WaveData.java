package io.github.zap.zombies.game.data2;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaveData {
    /**
     * time, in server ticks, before the wave's mobs are spawned
     */
    int waveLength = 100;

    /**
     * A list of the mobs that should be spawned during this wave
     */
    List<String> mobs = new ArrayList<>();

    public WaveData() {}
}
