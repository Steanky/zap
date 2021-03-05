package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Encapsulates some sort of Arena-managed player.
 */
@Getter
public abstract class ManagedPlayer<T extends ManagedPlayer<T, V>, V extends ManagingArena<V,T>> implements Unique,
        Disposable {
    private final V arena;
    private final ArenaPlayer arenaPlayer;
    private boolean inGame = true;

    public ManagedPlayer(V arena, ArenaPlayer arenaPlayer) {
        this.arena = arena;
        this.arenaPlayer = arenaPlayer;
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
        return arenaPlayer.getPlayer().getUniqueId();
    }

    public Player getPlayer() {
        return arenaPlayer.getPlayer();
    }

    /**
     * Returns true if the player is currently in the arena. Returns false otherwise.
     * @return true if the player is in the arena, false otherwise
     */
    public boolean inGame() {
        return inGame;
    }

    /**
     * Called when the player leaves the arena.
     */
    public void quit() {
        if(inGame) {
            inGame = false;
            arenaPlayer.removeAllConditionsFor(arena.toString());
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
