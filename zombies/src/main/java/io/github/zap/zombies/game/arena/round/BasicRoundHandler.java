package io.github.zap.zombies.game.arena.round;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.RoundContext;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.arena.spawner.Spawner;
import io.github.zap.zombies.game.arena.spawner.ZombieCountChangedArgs;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic implementation of a {@link RoundHandler}
 */
public class BasicRoundHandler implements RoundHandler {

    private final StatsManager statsManager;

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull Spawner spawner;

    private final @NotNull MapData map;

    private final @NotNull Collection<@NotNull PowerUp> powerUps;

    private final @NotNull RoundContext currentRound = new RoundContext(new ArrayList<>(), new ArrayList<>(),
            new ArrayList<>());

    private int round = -1;

    public BasicRoundHandler(@NotNull StatsManager statsManager,
                             @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList, @NotNull Spawner spawner,
                             @NotNull MapData map, @NotNull Collection<@NotNull PowerUp> powerUps) {
        this.statsManager = statsManager;
        this.playerList = playerList;
        this.spawner = spawner;
        this.map = map;
        this.powerUps = powerUps;

        spawner.getZombieCountChangedEvent().registerHandler(args -> {
            if (args.to() == 0) {
                checkNextRound();
            }
        });
    }

    @Override
    public void onGameBegin() {

    }

    @Override
    public void onZombieCountChanged(@NotNull ZombieCountChangedArgs args) {
        if (args.to() == 0 && state == ZombiesArenaState.STARTED) {

        }
    }

    private void checkNextRound() {
        if (state == ZombiesArenaState.STARTED) {
            doRound(++round);
        }
    }

    private void doRound(int targetRound) {
        currentRound.reset();

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
            RoundData nextRound = rounds.get(targetRound);
            spawner.spawnRound(nextRound);

            for (ZombiesPlayer player : playerList.getOnlinePlayers()) {
                Component title = nextRound.getCustomMessage() != null && !nextRound.getCustomMessage().isEmpty()
                        ? Component.text(nextRound.getCustomMessage())
                        : Component.text("ROUND " + (targetRound + 1), NamedTextColor.RED);
                player.getPlayer().showTitle(Title.title(title, Component.empty()));
                player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.wither.spawn"), Sound.Source.MASTER,
                        1.0F, 0.5F));
            }

            if (map.getDisablePowerUpRound().contains(targetRound + 1)) {
                for (@NotNull PowerUp powerUp : powerUps) {
                    if (powerUp.getState() == PowerUpState.NONE || powerUp.getState() == PowerUpState.DROPPED) {
                        powerUp.deactivate();
                    }
                }
            }
        }
        else {
            //game just finished, do win condition
            state = ZombiesArenaState.ENDED;
            doVictory();
        }
    }

}
