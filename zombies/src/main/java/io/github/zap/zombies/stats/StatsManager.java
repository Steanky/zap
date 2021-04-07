package io.github.zap.zombies.stats;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.stats.game.ZombiesPlayerStats;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Stats manager for Zombies
 */
public abstract class StatsManager {

    private final Queue<UUID> playerQueue = new ConcurrentLinkedQueue<>();

    private final Map<UUID, List<Consumer<ZombiesPlayerStats>>> playerTaskListMap = new ConcurrentHashMap<>();

    private final Thread taskThread = new Thread() {

        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (this) {
                    waitIfNothingEnqueued();
                    processNextPlayer();
                }
            }
        }

        private void waitIfNothingEnqueued() {
            if (playerQueue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Zombies.warning("StatsManager thread interrupted while waiting!");
                }
            }
        }

        private void processNextPlayer() {
            UUID next = playerQueue.poll();

            if (next != null) {
                List<Consumer<ZombiesPlayerStats>> tasks = playerTaskListMap.remove(next);

                if (tasks != null) {
                    processPlayer(tasks, next);
                }
            }
        }

        private void processPlayer(@NotNull List<Consumer<ZombiesPlayerStats>> tasks, @NotNull UUID uuid) {
            ZombiesPlayerStats stats = getStatsFor(uuid);

            if (stats == null) {
                stats = new ZombiesPlayerStats(uuid);
            }

            for (Consumer<ZombiesPlayerStats> task : tasks) {
                task.accept(stats);
            }

            writeStats(stats);
        }

    };

    public StatsManager() {
        taskThread.start();
    }

    /**
     * Enqueues a task to modify stats
     * @param targetPlayer The player to modify stats for
     * @param task The task to execute to modify the player stats
     */
    public void enqueueTask(@NotNull Player targetPlayer, @NotNull Consumer<ZombiesPlayerStats> task) {
        boolean initiallyEmpty = playerQueue.isEmpty();

        UUID uuid = targetPlayer.getUniqueId();

        if (!playerTaskListMap.containsKey(uuid)) {
            playerQueue.add(uuid);

            playerTaskListMap.put(uuid, new ArrayList<>() {
                {
                    add(task);
                }
            });
        } else {
            playerTaskListMap.get(uuid).add(task);
        }

        if (initiallyEmpty) {
            taskThread.notifyAll();
        }
    }

    /**
     * Gets the stats for a player UUID
     * @param uuid The uuid of the player to get stats for
     * @return The stats of the player
     */
    protected abstract @Nullable ZombiesPlayerStats getStatsFor(@NotNull UUID uuid);

    /**
     * Writes player stats
     * @param stats The stats of the player
     */
    protected abstract void writeStats(@NotNull ZombiesPlayerStats stats);

}
