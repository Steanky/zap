package io.github.zap.zombies.game2.arena.player;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class BasicHiddenPlayersManager implements HiddenPlayersManager {

    private final Map<UUID, Supplier<Player>> hiddenPlayers = new HashMap<>();

    private final Plugin plugin;

    public BasicHiddenPlayersManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addPlayer(@NotNull Supplier<Player> player) {
        Player toHide = player.get();
        hiddenPlayers.put(toHide.getUniqueId(), player);

        for (Player otherPlayer : toHide.getWorld().getPlayers()) {
            otherPlayer.hidePlayer(plugin, toHide);
        }
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
        for (Supplier<Player> hidden : hiddenPlayers.values()) {
            player.showPlayer(plugin, hidden.get());
        }
    }

    @Override
    public void hideHiddenPlayersForPlayer(@NotNull Player player) {
        for (Supplier<Player> hidden : hiddenPlayers.values()) {
            player.hidePlayer(plugin, hidden.get());
        }
    }

}
