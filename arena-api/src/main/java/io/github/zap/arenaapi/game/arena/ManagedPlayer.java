package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    private Player player;

    @Getter
    private boolean inGame = true;

    public ManagedPlayer(V arena, Player player) {
        this.arena = arena;
        this.player = player;
        this.playerUuid = player.getUniqueId();
    }

    /**
     * Gets the player held by this ManagedPlayer. Returns null if the player is offline, or not currently in this game.
     */
    public @Nullable Player getPlayer() {
        if(player != null) { //check if we have the player stored
            if(player.isOnline()) { //they're online, return the store
                return player;
            }
            else {
                player = null; //player is offline
                return null;
            }
        }
        else {
            return Bukkit.getPlayer(playerUuid);
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ManagedPlayer) {
            return ((ManagedPlayer<?,?>) obj).getId().equals(getId());
        }

        return false;
    }

    @Override
    public UUID getId() {
        return player.getUniqueId();
    }

    /**
     * Called when the player leaves the arena.
     */
    public void quit() {
        if(inGame) {
            Player player = getPlayer();
            if(player != null) {
                player.getInventory().clear();
                player.setExp(0);
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
