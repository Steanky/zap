package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
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
    protected final V arena;
    protected final Player player;
    private boolean inGame = true;

    public ManagedPlayer(V arena, Player player) {
        this.arena = arena;
        this.player = player;
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
            inGame = false;

            if(player.isOnline()) {
                player.getInventory().clear();
                player.setExp(0);
                player.teleport(getArena().getManager().getHubLocation());
                player.updateInventory();
                ArenaApi.getInstance().applyDefaultCondition(player);
            }
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
