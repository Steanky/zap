package io.github.zap.zombies.game.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaveData {
    /**
     * time, in server ticks, before the wave's mobs are spawned
     */
    int waveLength;

    /**
     * A list of the mobs that should be spawned during this wave
     */
    final List<String> mobs = new ArrayList<>();

    private WaveData() {}
}