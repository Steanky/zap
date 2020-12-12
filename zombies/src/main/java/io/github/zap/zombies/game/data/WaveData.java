package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.Serialize;
import io.github.zap.arenaapi.serialize.TypeAlias;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("ZombiesWave")
public class WaveData extends DataSerializable {
    /**
     * time, in server ticks, before the wave's mobs are spawned
     */
    int waveLength;

    /**
     * A list of the mobs that should be spawned during this wave
     */
    @Serialize(isAggregation = true)
    final ArrayList<MythicMob> mobs = new ArrayList<>();

    /**
     * bosses that will spawn during this wave
     */
    final ArrayList<BossSpawnpoint> bosses = new ArrayList<>();

    private WaveData() {}
}