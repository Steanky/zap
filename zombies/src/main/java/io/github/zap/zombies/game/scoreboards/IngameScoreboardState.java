package io.github.zap.zombies.game.scoreboards;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.netty.handler.logging.LogLevel;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Zombie;
import org.bukkit.scoreboard.Scoreboard;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static io.github.zap.zombies.game.scoreboards.GameScoreboard.DATE_FORMATTER;

public class IngameScoreboardState implements IGameScoreboardState, Disposable {
    private GameScoreboard gameScoreboard;

    private Map<UUID, ImmutablePair<TextFragment, TextFragment>> playerStatues = new HashMap<>();
    private Map<UUID, Triple<Scoreboard, SidebarTextWriter, TextFragment>> playScoreboards = new HashMap<>();

    private TextFragment round = new TextFragment();
    private TextFragment zombieLeft = new TextFragment();
    private TextFragment time = new TextFragment();


    @Override
    public void stateChangedFrom(ZombiesArenaState gameState, GameScoreboard scoreboard) {
        this.gameScoreboard = scoreboard;
        var date = DATE_FORMATTER.format(LocalDateTime.now());
        var map = scoreboard.getZombiesArena().getMap().getName();

        for(var i : scoreboard.getZombiesArena().getPlayerMap().entrySet()) {
            var tfName = new TextFragment(i.getValue().getPlayer().getName());
            var tfState = new TextFragment();
            playerStatues.put(i.getKey(), ImmutablePair.of(tfName, tfState));
        }

        for(var player : scoreboard.getZombiesArena().getPlayerMap().entrySet()) {
            var bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            var writer = SidebarTextWriter.create(bukkitScoreboard, GameScoreboard.SIDEBAR_TITLE);
            var zombieKills = new TextFragment(ChatColor.GOLD + "0");

            writer.line(ChatColor.GRAY, date)
                  .line()
                  .line("" + ChatColor.BOLD + ChatColor.RED + "Round ", round)
                  .line("Zombies Left: " + ChatColor.GREEN, zombieLeft)
                  .line();

            playerStatues.forEach((l,r) -> writer.line(ChatColor.GRAY, r.left, ChatColor.WHITE + ": ", r.right));

            writer.line()
                  .line("Zombie Kills: " + ChatColor.GREEN, zombieKills)
                  .line("Time: " + ChatColor.GREEN, time)
                  .line("Map: " + ChatColor.GREEN + map)
                  .line()
                  .text(ChatColor.YELLOW + "discord.gg/:zzz:");

            playScoreboards.put(player.getKey(),Triple.of(bukkitScoreboard, writer, zombieKills));

            // Add their in game scoreboard for every player still in the game
            if(player.getValue().isInGame())
                player.getValue().getPlayer().setScoreboard(bukkitScoreboard);
        }

        scoreboard.getZombiesArena().getPlayerJoinEvent().registerHandler(this::handleRejoin);
        scoreboard.getZombiesArena().getPlayerLeaveEvent().registerHandler(this::handleLeave);
    }

    private void handleLeave(ManagingArena<ZombiesArena, ZombiesPlayer>.ManagedPlayerListArgs managedPlayerListArgs) {
        managedPlayerListArgs.getPlayers().forEach(x -> x.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()));
    }

    private void handleRejoin(ManagingArena.PlayerListArgs playerListArgs) {
        for(var player : playerListArgs.getPlayers()) {
            if(playScoreboards.containsKey(player.getUniqueId())) {
                player.setScoreboard(playScoreboards.get(player.getUniqueId()).getLeft());
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find scoreboard for player: " + player.getName() + " with UUID: " + player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Unable to load your scoreboard!");
            }
        }
    }


    @Override
    public void update() {
        // Update general information
        var playerMap =  gameScoreboard.getZombiesArena().getPlayerMap();
        round.setValue(gameScoreboard.getZombiesArena().getMap().getCurrentRoundProperty().getValue(gameScoreboard.getZombiesArena()).toString());
        zombieLeft.setValue("" + gameScoreboard.getZombiesArena().getMobs().size());

        // Update player status
        for(var playerStatus : playerStatues.entrySet()) {
            var tfStatus = playerStatus.getValue().right;

            if(playerMap.containsKey(playerStatus.getKey())) {
                var player = playerMap.get(playerStatus.getKey());
                if(!player.isInGame()) {
                    tfStatus.setValue(ChatColor.RED + "QUIT");
                } else {
                    switch (player.getState()) {
                        case DEAD:
                            tfStatus.setValue(ChatColor.RED + "DEAD");
                            break;
                        case KNOCKED:
                            tfStatus.setValue(ChatColor.YELLOW + "REVIVE");
                            break;
                        case ALIVE:
                            tfStatus.setValue(ChatColor.GOLD + "" + player.getCoins());
                            break;
                    }
                }
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find player with UUID: " + playerStatus.getKey().toString());
                tfStatus.setValue(ChatColor.RED + "QUIT");
            }
        }

        // Update player kills and update the scoreboard
        for(var playerSb : playScoreboards.entrySet()) {
            if(playerMap.containsKey(playerSb.getKey())) {
                var player = playerMap.get(playerSb.getKey());
                playerSb.getValue().getRight().setValue("" + player.getKills());
                playerSb.getValue().getMiddle().update();
            } else {
                Zombies.getInstance().getLogger().log(Level.SEVERE, "Could not find player with UUID: " + playerSb.getKey().toString());
            }
        }
    }

    @Override
    public void dispose() {
        gameScoreboard.getZombiesArena().getPlayerJoinEvent().removeHandler(this::handleRejoin);
        gameScoreboard.getZombiesArena().getPlayerLeaveEvent().removeHandler(this::handleLeave);
        playScoreboards.forEach((uuid, data) -> data.getMiddle().dispose());
    }
}
