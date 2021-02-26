package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.github.zap.zombies.game.scoreboards.GameScoreboard.DATE_FORMATTER;

public class PregameScoreboardState implements GameScoreboardState, Disposable {
    public static final List<Integer> CD_MILESTONE = Arrays.asList(20, 10, 5, 4, 3, 2, 1);
    private double counter = 21;

    private GameScoreboard gameScoreboard;
    private Scoreboard bukkitScoreboard;

    private SidebarTextWriter writer;

    private StringFragment tfPlayerCount = new StringFragment();
    private StringFragment cd = new StringFragment();
    private StringFragment status = new StringFragment();

    @Override
    public void stateChangedFrom(ZombiesArenaState gameState, GameScoreboard gameScoreboard) {
        this.gameScoreboard = gameScoreboard;
        this.bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        var date = DATE_FORMATTER.format(LocalDateTime.now());
        var mapInfo = gameScoreboard.getZombiesArena().getMap();


        writer =  SidebarTextWriter.create(bukkitScoreboard, GameScoreboard.SIDEBAR_TITLE);
        writer.clear();
        writer.line(ChatColor.GRAY, date)
              .line()
              .line("Map: ", ChatColor.GREEN, mapInfo.getName())
              .line("Players: ", ChatColor.GREEN, tfPlayerCount, "/", mapInfo.getMaximumCapacity())
              .line()
              .line(status, ChatColor.GREEN + " ", cd)
              .line()
              .text(ChatColor.YELLOW + "discord.gg/private-bro");

        for (var player : gameScoreboard.getZombiesArena().getPlayerMap().values()){
            player.getPlayer().setScoreboard(bukkitScoreboard);
        }
        
        gameScoreboard.getZombiesArena().getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        gameScoreboard.getZombiesArena().getPlayerLeaveEvent().registerHandler(this::onPlayerLeave);
    }

    private void onPlayerLeave(ManagingArena<ZombiesArena, ZombiesPlayer>.ManagedPlayerListArgs managedPlayerListArgs) {
        for(ZombiesPlayer player : managedPlayerListArgs.getPlayers()) {
            player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }


    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        for(Player player : playerListArgs.getPlayers()) {
            player.setScoreboard(bukkitScoreboard);
        }
    }



    @Override
    public void update() {
        var arena = gameScoreboard.getZombiesArena();

        tfPlayerCount.setValue("" + arena.getPlayerMap().size());
        switch (arena.getState()) {
            case PREGAME:
                if(counter != 21) {
                    arena.getPlayerMap().forEach((l,r) -> {
                        r.getPlayer().sendMessage(ChatColor.YELLOW + "Not enough player to start the game, countdown canceled");
                        r.getPlayer().playSound(r.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                    });

                    counter = 21;
                }

                status.setValue("Waiting...");
                cd.setValue("");
                break;
            case COUNTDOWN:
                status.setValue("Starting in");
                var lastDisplayCounter = (int) Math.ceil(counter);
                counter = counter > 20 ? 20 : counter - gameScoreboard.getRefreshRate() / 20f;
                var displayCounter = (int) Math.ceil(counter);

                if(CD_MILESTONE.contains(displayCounter) && lastDisplayCounter != displayCounter)  {
                    arena.getPlayerMap().forEach((l,r) -> {
                        r.getPlayer().sendMessage(ChatColor.YELLOW + "The game starts in " + displayCounter + " seconds!");
                        r.getPlayer().playSound(r.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                    });
                }

                cd.setValue("" + displayCounter + "s");
                if(counter <= 0) {
                    arena.startGame();
                }
                break;
        }

        writer.update();
    }

    @Override
    public void dispose() {
        gameScoreboard.getZombiesArena().getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
        writer.dispose();
    }
}
