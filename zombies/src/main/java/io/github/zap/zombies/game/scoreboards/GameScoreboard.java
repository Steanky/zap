package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class GameScoreboard extends BukkitRunnable implements Disposable {
    // Should these be in a config file?
    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");
    public static String SIDEBAR_TITLE = "" + ChatColor.YELLOW + ChatColor.BOLD + "Zombies";
    private static Map<ZombiesArenaState, Class<? extends IGameScoreboardState>> SCOREBOARD_STATES;
    static {
        SCOREBOARD_STATES = new HashMap<>();
        SCOREBOARD_STATES.put(ZombiesArenaState.PREGAME, PregameScoreboardState.class);
        SCOREBOARD_STATES.put(ZombiesArenaState.COUNTDOWN, PregameScoreboardState.class);
        SCOREBOARD_STATES.put(ZombiesArenaState.STARTED, IngameScoreboardState.class);
        SCOREBOARD_STATES.put(ZombiesArenaState.ENDED, IngameScoreboardState.class);
    }

    @Getter
    private final ZombiesArena zombiesArena;

    @Getter
    private final int refreshRate;

    private IGameScoreboardState currentState;

    private ZombiesArenaState previousState;

    private BukkitTask updateTask;

    public GameScoreboard(ZombiesArena zombiesArena) {
        this(zombiesArena, 10);
    }

    public GameScoreboard(ZombiesArena zombiesArena, int refreshRate) {
        this.zombiesArena = zombiesArena;
        this.refreshRate = refreshRate;
    }

    private IGameScoreboardState getCurrentState() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var arenaState = getZombiesArena().getState();
        if(arenaState != previousState) {
            currentState = SCOREBOARD_STATES.get(arenaState).getConstructor().newInstance();
            currentState.stateChangedFrom(previousState, this);
            previousState = arenaState;
        }

        return currentState;
    }

    public void initialize() {
        if(updateTask == null)
            updateTask = this.runTaskTimer(Zombies.getInstance(), 0, refreshRate);
    }

    @Override
    public void run() {
        try {
            getCurrentState().update();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            Zombies.log(Level.SEVERE, "Cannot instantiate game scoreboard state! ");
        }
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
