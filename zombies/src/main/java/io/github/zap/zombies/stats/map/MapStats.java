package io.github.zap.zombies.stats.map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stats for a Zombies map
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapStats {

    public MapStats(String mapName) {
        this.mapName = mapName;
    }

    private MapStats() {

    }

    String mapName;

    List<Pair<UUID, Integer>> bestTimes = new ArrayList<>(); // TODO: better collection?

}
