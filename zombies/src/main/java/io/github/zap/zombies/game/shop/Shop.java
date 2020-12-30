package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Represents a station at which items are purchasable
 * @param <D> The data type of the shop
 */
@Getter
public abstract class Shop<D extends ShopData> {

    private final LocalizationManager localizationManager;

    private final ZombiesArena zombiesArena;

    private final D shopData;

    private boolean powered = false;

    public Shop(ZombiesArena zombiesArena, D shopData) {
        this.localizationManager = Zombies.getInstance().getLocalizationManager();
        this.zombiesArena = zombiesArena;
        this.shopData = shopData;

        registerArenaEvents();
    }

    /**
     * Registers all events from the zombie arena that will be monitored by the shop
     */
    protected void registerArenaEvents() {
        zombiesArena.getShopEvents().get(ShopType.POWER_SWITCH.name()).registerHandler(args -> {
            powered = true;
            display();
        });
        zombiesArena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
    }

    /**
     * Called when a player joins the arena
     * @param args The list of players
     */
    protected void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        for (Player player : args.getPlayers()) {
            displayTo(player);
        }
    }

    /**
     * Method to call when a player purchases an item
     * @param zombiesPlayer The purchasing player
     */
    protected void onPurchaseSuccess(ZombiesPlayer zombiesPlayer) {
        zombiesArena.getShopEvents().get(getShopType()).callEvent(new ShopEventArgs(this, zombiesPlayer));
    }

    /**
     * Displays the shop to all players in its current state
     */
    public void display() {
        for (Player player : zombiesArena.getWorld().getPlayers()) {
            displayTo(player);
        }
    }

    /**
     * Displays the shop to a single player
     * @param player THe player to display the shop to
     */
    protected abstract void displayTo(Player player);

    /**
     * Attempts to purchase an item for a player
     * @param args The event called that could cause a shop's interaction
     * @return Whether the purchase was successful
     */
    protected abstract boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args);

    /**
     * Gets the type of the shop
     * @return A string representation of the type of the shop
     */
    public abstract String getShopType();

}
