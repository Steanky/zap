package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Encapsulates some sort of Arena-managed player.
 */
public abstract class ManagedPlayer<T extends ManagedPlayer<T, V>, V extends ManagingArena<V,T>> implements Unique,
        Disposable {
    @Getter
    protected final V arena;

    private final UUID playerUuid;

    @Getter
    private boolean inGame = true;

    public ManagedPlayer(@NotNull V arena, @NotNull Player player) {
        this.arena = arena;
        this.playerUuid = player.getUniqueId();
    }

    /**
     * Gets the player held by this ManagedPlayer. Returns null if the player is offline, or not currently in this game.
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(playerUuid);
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(playerUuid);
    }


    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ManagedPlayer<?, ?> player) {
            return player.getId().equals(getId());
        }

        return false;
    }

    @Override
    public UUID getId() {
        return playerUuid;
    }

    /**
     * Called when the player leaves the arena.
     */
    public void quit() {
        if(inGame) {
            Player player = getPlayer();
            if(player != null) {
                player.getInventory().clear();
                player.updateInventory();
                ArenaApi.getInstance().applyDefaultCondition(player);
                ArenaApi.getInstance().evacuatePlayer(getArena(), player);
            }

            inGame = false;
        }
    }

    /**
     * Called when the player rejoins the arena. This occurs before playerRejoinEvent is fired.
     */
    public void rejoin() {
        if(!inGame) {
            inGame = true;
        }
    }
}
