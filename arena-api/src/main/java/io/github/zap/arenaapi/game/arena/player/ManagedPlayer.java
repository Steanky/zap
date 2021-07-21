package io.github.zap.arenaapi.game.arena.player;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Encapsulates some sort of Arena-managed player.
 */
public abstract class ManagedPlayer implements Unique, Disposable {

    private final OfflinePlayer player;

    @Getter
    private boolean inGame = true;

    public ManagedPlayer(@NotNull OfflinePlayer player) {
        this.player = player;
    }

    /**
     * Gets the player held by this ManagedPlayer.
     * Throws {@link IllegalStateException} if the player is not online.
     * @return The online player
     */
    public @NotNull Player getPlayer() {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer == null) {
            throw new IllegalStateException("Player " + player.getName() + " is not online!");
        }

        return bukkitPlayer;
    }

    /**
     * Gets the player held by this ManagedPlayer.
     * @return The offline player
     */
    public @NotNull String getPlayerName() {
        String playerName = player.getName();
        return (playerName == null) ? "Unknown Player" : playerName;
    }


    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ManagedPlayer player) {
            return player.getId().equals(getId());
        }

        return false;
    }

    @Override
    public @NotNull UUID getId() {
        return player.getUniqueId();
    }

    /**
     * Called when the player leaves the arena.
     */
    public void quit() {
        if (inGame) {
            Player bukkitPlayer = getPlayer();
            bukkitPlayer.getInventory().clear();
            bukkitPlayer.updateInventory();
            ArenaApi.getInstance().applyDefaultCondition(bukkitPlayer);

            inGame = false;
        }
    }

    /**
     * Called when the player rejoins the arena. This occurs before playerRejoinEvent is fired.
     */
    public void rejoin() {
        if (!inGame) {
            inGame = true;
        }
    }

}
