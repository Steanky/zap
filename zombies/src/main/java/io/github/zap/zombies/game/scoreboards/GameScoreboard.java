package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GameScoreboard extends BukkitRunnable implements Disposable {
    // Should these be in a config file?
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");
    public static final String SIDEBAR_TITLE = "" + ChatColor.YELLOW + ChatColor.BOLD + "Zombies";
    private static final Map<ZombiesArenaState, Supplier<GameScoreboardState>> SCOREBOARD_STATES;
    static {
        SCOREBOARD_STATES = new HashMap<>();
        SCOREBOARD_STATES.put(ZombiesArenaState.PREGAME, PregameScoreboardState::new);
        SCOREBOARD_STATES.put(ZombiesArenaState.COUNTDOWN, PregameScoreboardState::new);
        SCOREBOARD_STATES.put(ZombiesArenaState.STARTED, IngameScoreboardState::new);
        SCOREBOARD_STATES.put(ZombiesArenaState.ENDED, IngameScoreboardState::new);
    }

    @Getter
    private final ZombiesArena zombiesArena;

    @Getter
    private final int refreshRate;

    private GameScoreboardState currentState;

    private ZombiesArenaState previousState;

    private BukkitTask updateTask;

    public GameScoreboard(ZombiesArena zombiesArena) {
        this(zombiesArena, 10);
    }

    public GameScoreboard(ZombiesArena zombiesArena, int refreshRate) {
        this.zombiesArena = zombiesArena;
        this.refreshRate = refreshRate;
    }

    private GameScoreboardState getCurrentState() {
        var arenaState = getZombiesArena().getState();
        if(arenaState != previousState) {
            currentState = SCOREBOARD_STATES.get(arenaState).get();
            currentState.stateChangedFrom(previousState, this);
            previousState = arenaState;

            if (arenaState == ZombiesArenaState.ENDED) {
                for (ZombiesPlayer zombiesPlayer : getZombiesArena().getPlayerMap().values()) {
                    zombiesPlayer.knock();
                }
            }
        }

        return currentState;
    }

    public void initialize() {
        if(updateTask == null)
            updateTask = this.runTaskTimer(Zombies.getInstance(), 0, refreshRate);
    }

    @Override
    public void run() {
        getCurrentState().update();
    }

    @Override
    public void dispose() {
        // stop the update task
        if(updateTask != null && !updateTask.isCancelled())
            updateTask.cancel();

        if(currentState != null && currentState instanceof Disposable) {
            ((Disposable) currentState).dispose();
        }
    }
}
