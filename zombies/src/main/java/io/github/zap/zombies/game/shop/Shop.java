package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.visual.ShopVisual;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a station at which items are purchasable
 * @param <D> The data type of the shop
 */
@Getter
public abstract class Shop<D extends @NotNull ShopData> {

    private final @NotNull World world;

    private final @NotNull ShopEventManager eventManager;

    private final D shopData;

    private boolean powered = false;

    public Shop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull D shopData) {
        this.world = world;
        this.eventManager = eventManager;
        this.shopData = shopData;

        registerArenaEvents();
        registerShopEvents(eventManager);
    }

    /**
     * Registers all events from the zombie arena that will be monitored by the shop
     */
    protected void registerArenaEvents() {
        arena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
        arena.getPlayerRejoinEvent().registerHandler(this::onPlayerRejoin);
    }

    /**
     * Registers all events related to other shops
     * @param eventManager The event manager to register shop events to
     */
    protected void registerShopEvents(@NotNull ShopEventManager eventManager) {
        eventManager.getEvent(ShopType.POWER_SWITCH.name()).registerHandler(args -> {
            powered = true;
            display();
        });
    }

    /**
     * Called when players join the {@link ZombiesArena}
     * @param players The list of players
     */
    protected void onPlayerJoin(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            displayToPlayer(player);
        }
    }

    /**
     * Called when players rejoin the arena
     * @param players The list of players
     */
    protected void onPlayerRejoin(@NotNull List<? extends @NotNull ZombiesPlayer> players) {
        for (ZombiesPlayer player : players) {
            Player bukkitPlayer = player.getPlayer();
            displayToPlayer(bukkitPlayer);
        }
    }

    /**
     * Method to call when a player purchases an item
     * @param player The purchasing player
     */
    protected void onPurchaseSuccess(@NotNull ZombiesPlayer player) {
        eventManager.getEvent(getShopType()).callEvent(new ShopEventArgs<>(this, player));
    }

    /**
     * Displays the shop to all players in its current state
     */
    public void display() {
        for (Player player : world.getPlayers()) {
            displayToPlayer(player);
        }
    }

    /**
     * Displays the shop to a single player
     * @param player THe player to display the shop to
     */
    protected void displayToPlayer(Player player) {

    }

    /**
     * Attempts to purchase an item for a player
     * @param args The event called that could cause a shop's interaction
     * @return Whether an interaction occurred
     */
    public abstract boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args);

    /**
     * Gets the type of the shop
     * @return A representation of the type of the shop
     */
    public abstract String getShopType();
}
