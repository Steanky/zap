package io.github.zap.zombies.stats.map;

import io.github.zap.arenaapi.stats.Stats;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stats for a Zombies map
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapStats extends Stats<String> {

    public MapStats(String mapName) {
        super(mapName);
    }

    private MapStats() {

    }

    List<Pair<UUID, Integer>> bestTimes = new ArrayList<>(); // TODO: better collection?

}
