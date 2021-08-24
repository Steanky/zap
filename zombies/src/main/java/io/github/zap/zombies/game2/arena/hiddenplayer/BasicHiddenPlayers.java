package io.github.zap.zombies.game2.arena.hiddenplayer;

import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicHiddenPlayers implements HiddenPlayers {

    private final Map<UUID, PlayerView> hiddenPlayers = new HashMap<>();

    private final Plugin plugin;

    public BasicHiddenPlayers(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addPlayer(@NotNull PlayerView playerView) {
        hiddenPlayers.put(playerView.getUUID(), playerView);

        playerView.getPlayerIfValid().ifPresent(player -> {
            for (Player otherPlayer : player.getWorld().getPlayers()) {
                otherPlayer.hidePlayer(plugin, player);
            }
        });
    }

    @Override
    public void removePlayer(@NotNull Player player) {
        hiddenPlayers.remove(player.getUniqueId());

        for (Player otherPlayer : player.getWorld().getPlayers()) {
            otherPlayer.showPlayer(plugin, player);
        }
    }

    @Override
    public void showHiddenPlayersToPlayer(@NotNull Player player) {
        for (PlayerView playerView : hiddenPlayers.values()) {
            playerView.getPlayerIfValid().ifPresent(hiddenPlayer -> player.showPlayer(plugin, hiddenPlayer));
        }
    }

    @Override
    public void hideHiddenPlayersForPlayer(@NotNull Player player) {
        for (PlayerView playerView : hiddenPlayers.values()) {
            playerView.getPlayerIfValid().ifPresent(hiddenPlayer -> player.showPlayer(plugin, hiddenPlayer));
        }
    }

    @Override
    public void unhideAll() {
        for (PlayerView playerView : hiddenPlayers.values()) {
            playerView.getPlayerIfValid().ifPresent(hiddenPlayer -> {
                for (Player player : hiddenPlayer.getWorld().getPlayers()) {
                    player.showPlayer(plugin, hiddenPlayer);
                }
            });
        }
    }

}
