package io.github.zap.zombies.game.arena.round;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.RoundContext;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.arena.spawner.Spawner;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoundData;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.WaveData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.github.zap.zombies.stats.CacheInformation;
import io.github.zap.zombies.stats.player.PlayerGeneralStats;
import io.github.zap.zombies.stats.player.PlayerMapStats;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic implementation of a {@link RoundHandler}
 */
public class BasicRoundHandler implements RoundHandler {

    private final @NotNull MapData map;

    private final @NotNull Spawner spawner;

    public BasicRoundHandler(@NotNull MapData map, @NotNull Spawner spawner) {
        this.map = map;
        this.spawner = spawner;

        spawner.getZombieCountChangedEvent().registerHandler(args -> {
            if (args.to() == 0) {
                checkNextRound();
            }
        });
    }

    private void checkNextRound() {
        if (state == ZombiesArenaState.STARTED) {
            Property<Integer> currentRound = map.getCurrentRoundProperty();
            doRound(currentRound.getValue(this) + 1);
        }
    }

    public void doRound(int targetRound) {
        RoundContext context = new RoundContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        if(currentRound != null) {
            currentRound.cancelRound();
        }

        currentRound = context;

        Property<Integer> roundIndexProperty = map.getCurrentRoundProperty();

        int lastRoundIndex = targetRound - 1;
        int secondsElapsed = (int) ((System.currentTimeMillis() - startTimeStamp) / 1000);
        for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
            if (!player.isAlive()) {
                player.respawn();
            }

            if (map.getRoundTimesShouldSave().contains(lastRoundIndex)) {
                statsManager.queueCacheModification(CacheInformation.PLAYER, player.getId(), (stats) -> {
                    PlayerMapStats mapStats = stats.getMapStatsForMap(map);
                    mapStats.setRoundsSurvived(mapStats.getRoundsSurvived() + 1);

                    if (mapStats.getBestRound() < lastRoundIndex) {
                        mapStats.setBestRound(lastRoundIndex);
                    }

                    Map<Integer, Integer> bestTimes = mapStats.getBestTimes();
                    Integer bestTime = bestTimes.get(lastRoundIndex);
                    if (bestTime == null || bestTime < secondsElapsed) {
                        bestTimes.put(lastRoundIndex, secondsElapsed);
                    }
                }, PlayerGeneralStats::new);
            } else {
                statsManager.queueCacheModification(CacheInformation.PLAYER, player.getId(), (stats) -> {
                    PlayerMapStats mapStats = stats.getMapStatsForMap(map);
                    mapStats.setRoundsSurvived(mapStats.getRoundsSurvived() + 1);

                    if (mapStats.getBestRound() < lastRoundIndex) {
                        mapStats.setBestRound(lastRoundIndex);
                    }
                }, PlayerGeneralStats::new);
            }
        }

        List<RoundData> rounds = map.getRounds();
        if (targetRound < rounds.size()) {
            RoundData currentRound = rounds.get(targetRound);


        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

}
