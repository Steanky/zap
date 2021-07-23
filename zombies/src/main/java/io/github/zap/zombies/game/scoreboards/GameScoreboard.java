package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Supplier;

public class GameScoreboard implements Disposable, Runnable {
    // Should these be in a config file?
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");
    public static final String SIDEBAR_TITLE = "" + ChatColor.YELLOW + ChatColor.BOLD + "Zombies";

    private final @NotNull Map<@NotNull ZombiesArenaState, @NotNull Supplier<@NotNull GameScoreboardState>> scoreboardStates;

    private final @NotNull BukkitTaskManager taskManager;

    @Getter
    private final int refreshRate;

    private GameScoreboardState currentState;

    private ZombiesArenaState previousState;

    private BukkitTask updateTask;

    private boolean isDisposed;

    public GameScoreboard(@NotNull Map<@NotNull ZombiesArenaState, @NotNull Supplier<@NotNull GameScoreboardState>> scoreboardStates,
                          @NotNull BukkitTaskManager taskManager) {
        this(scoreboardStates, taskManager, 10);
    }

    public GameScoreboard(@NotNull Map<@NotNull ZombiesArenaState, @NotNull Supplier<@NotNull GameScoreboardState>> scoreboardStates,
                          @NotNull BukkitTaskManager taskManager, int refreshRate) {
        this.scoreboardStates = scoreboardStates;
        this.taskManager = taskManager;
        this.refreshRate = refreshRate;
    }

    private GameScoreboardState getCurrentState() {
        var arenaState = getZombiesArena().getState();
        if (arenaState != previousState) {
            currentState = scoreboardStates.get(arenaState).get();
            currentState.stateChangedFrom(previousState, this);
            previousState = arenaState;
        }

        return currentState;
    }

    public void initialize() {
        if (updateTask == null) {
            updateTask = taskManager.runTaskTimer(0L, refreshRate, this);
        }
    }

    @Override
    public void run() {
        getCurrentState().update();
    }


    @Override
    public void dispose() {
        if(isDisposed)   return;
        isDisposed = true;
        // stop the update task
        if(updateTask != null && !updateTask.isCancelled())
            updateTask.cancel();

        for (ZombiesPlayer zombiesPlayer : zombiesArena.getPlayerMap().values()) {
            Player player = zombiesPlayer.getPlayer();
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        if(currentState != null && currentState instanceof Disposable disposable) {
            disposable.dispose();
        }
    }
}
