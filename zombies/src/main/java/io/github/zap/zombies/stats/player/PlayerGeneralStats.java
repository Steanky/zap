package io.github.zap.zombies.stats.player;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerGeneralStats {

    public PlayerGeneralStats(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @SuppressWarnings("unused")
    private PlayerGeneralStats() {

    }

    UUID uuid;

    int bulletsShot;

    int bulletsHit;

    int headShots;

    Map<String, PlayerMapStats> mapStatsMap = new HashMap<>();

}
