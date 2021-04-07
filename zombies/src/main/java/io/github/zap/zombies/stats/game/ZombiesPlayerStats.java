package io.github.zap.zombies.stats.game;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZombiesPlayerStats {

    public ZombiesPlayerStats(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @SuppressWarnings("unused")
    private ZombiesPlayerStats() {

    }

    UUID uuid;

    Map<String, MapStats> mapStatsMap = new HashMap<>();

}
