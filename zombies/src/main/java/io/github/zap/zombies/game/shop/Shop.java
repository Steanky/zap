package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Getter
public abstract class Shop<D extends ShopData> {

    private final ZombiesArena zombiesArena;

    private final D shopData;

    private boolean powered = false;

    public Shop(ZombiesArena zombiesArena, D shopData) {
        this.zombiesArena = zombiesArena;
        this.shopData = shopData;

        registerArenaEvents();
    }

    protected void registerArenaEvents() {
        zombiesArena.getShopEvents().get(ShopType.POWER_SWITCH.name()).registerHandler(args -> {
            powered = true;
            display();
        });
        zombiesArena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);
    }

    public void onPlayerJoin(ManagingArena.PlayerListArgs args) {
        for (Player player : args.getPlayers()) {
            displayTo(player);
        }
    }

    protected void onPurchaseSuccess(ZombiesPlayer zombiesPlayer) {
        zombiesArena.getShopEvents().get(getShopType()).callEvent(new ShopEventArgs(this, zombiesPlayer));
    }

    public void display() {
        for (Player player : zombiesArena.getWorld().getPlayers()) {
            displayTo(player);
        }
    }

    protected abstract void displayTo(Player player);

    protected abstract boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args);

    public abstract String getShopType();

}
