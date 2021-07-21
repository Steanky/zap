package io.github.zap.arenaapi.game.arena.player;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Basic implementation of a {@link PlayerList}
 * @param <S> The type of player this list handles
 */
public class BasicPlayerList<S extends ManagedPlayer> implements PlayerList<S> {

    private final Map<UUID, S> playerMap = new HashMap<>();

    private final ManagedPlayerBuilder<S> playerBuilder;

    private final Location evacuationLocation;

    public BasicPlayerList(@NotNull ManagedPlayerBuilder<S> playerBuilder, @NotNull Location evacuationLocation) {
        this.playerBuilder = playerBuilder;
        this.evacuationLocation = evacuationLocation;
    }

    @Override
    public @NotNull Map<@NotNull UUID, @NotNull S> getPlayerMap() {
        return Collections.unmodifiableMap(playerMap);
    }

    @Override
    public boolean addPlayer(@NotNull Player player) {
        if (canPlayerJoin(player)) {
            S managedPlayer = playerBuilder.wrapPlayer(player);
            playerMap.put(managedPlayer.getId(), managedPlayer);

            return true;
        }

        return false;
    }

    @Override
    public boolean removePlayer(@NotNull S player) {
        if (playerMap.containsKey(player.getId())) {
            player.dispose();
            if (player.isInGame()) {
                player.quit();
            }
            player.getPlayer();
            if (evacuationLocation.isWorldLoaded()) {
                player.getPlayer().teleportAsync(evacuationLocation);
            } else {
                player.getPlayer().kick(Component.text("Couldn't teleport you back to the hub!"));
            }

            playerMap.remove(player.getId());
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        for (S player : playerMap.values()) {
            removePlayer(player);
        }
    }

}
