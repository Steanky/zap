package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.Unique;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

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
            close();
        }
    }

    /**
     * Called when the player rejoins the arena
     */
    public void rejoin() {
        if(!inGame) {
            inGame = true;
            init();
        }
    }

    /**
     * Performs cleanup tasks. This is called when the arena shuts down or the player quits the game. The actions
     * performed by this function generally should be reversible, as the player may rejoin the game at a later date.
     */
    public void close() {}

    /**
     * Performs initialization tasks. This should fully reverse the effects of close() unless the game is designed
     * such that rejoining is penalized somehow.
     */
    public void init() {}

    /**
     * Called when this player interacts with a block or air.
     * @param event The PlayerInteractEvent
     */
    protected void onPlayerInteract(PlayerInteractEvent event) {}

    /**
     * Called when this player interacts with an entity.
     * @param event The PlayerInteractAtEntityEvent
     */
    protected void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {}

    /**
     * Called when this player sneaks.
     * @param event The PlayerToggleSneakEvent
     */
    protected void onPlayerSneak(PlayerToggleSneakEvent event) {}

    /**
     * Called when this player dies.
     * @param event The PlayerDeathEvent
     */
    protected void onPlayerDeath(PlayerDeathEvent event) {}
}
