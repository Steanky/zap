package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Encapsulates some sort of Arena-managed player
 */
@RequiredArgsConstructor
@Getter
public abstract class ManagedPlayer<T extends ManagedPlayer<T, V>, V extends ManagingArena<V,T>> implements Unique {
    private final V arena;
    private final Player player;
    private boolean inGame = true;

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
     * Returns true if the player is currently in the arena. Returns false otherwise.
     * @return true if the player is in the arena, false otherwise
     */
    public boolean inGame() {
        return inGame;
    }

    /**
     * Called when the player leaves the arena
     */
    public void quit() {
        if(inGame) {
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

    /**
     * Performs cleanup tasks. This is called when the arena shuts down, and is not reversible.
     */
    public void close() {}
}
