package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BasicShopManager implements ShopManager {

    private final @NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList;

    private final @NotNull ShopEventManager eventManager;

    private final @NotNull Collection<@NotNull Shop<@NotNull ?>> shops;

    public BasicShopManager(@NotNull PlayerList<? extends @NotNull ZombiesPlayer> playerList,
                            @NotNull ShopEventManager eventManager,
                            @NotNull Collection<@NotNull Shop<@NotNull ?>> shops) {
        this.playerList = playerList;
        this.eventManager = eventManager;
        this.shops = shops;

        eventManager.getEvent(ShopType.LUCKY_CHEST.name()).registerHandler(args -> {
            // TODO: implement
        });
    }


    @Override
    public boolean checkForPurchases(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        for (@NotNull Shop<@NotNull ?> shop : shops) {
            if (shop.interact(args)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void dispose() {
        eventManager.dispose();
        for (@NotNull Shop<@NotNull ?> shop : shops) {
            if (shop instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
    }
}
