package io.github.zap.zombies.game2.player.kills;

import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BasicKillManager implements KillManager {

    private final StatsManager statsManager;

    private final MapData map;

    private final UUID playerUUID;

    private int kills = 0;

    public BasicKillManager(@NotNull StatsManager statsManager, @NotNull MapData map, @NotNull UUID playerUUID) {
        this.statsManager = statsManager;
        this.map = map;
        this.playerUUID = playerUUID;
    }

    @Override
    public void addKills(int kills) {
        this.kills += kills;

        statsManager.queueCacheRequest(CacheInformation.PLAYER, playerUUID, PlayerGeneralStats::new, stats -> {
            PlayerMapStats mapStats = stats.getMapStatsForMap(map);
            mapStats.setKills(mapStats.getKills() + 1);
        });
    }

    @Override
    public int getKills() {
        return kills;
    }

}
