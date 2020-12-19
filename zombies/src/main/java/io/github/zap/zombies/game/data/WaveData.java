package io.github.zap.zombies.game.data;

import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

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
    transient final ArrayList<MythicMob> mobs = new ArrayList<>();

    private WaveData() {}
}