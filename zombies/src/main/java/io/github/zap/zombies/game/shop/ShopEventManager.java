package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ShopEventManager implements Disposable {

    private final @NotNull Map<@NotNull String, @NotNull Event<@NotNull ShopEventArgs<@NotNull ?>>> events = new HashMap<>();

    private final @NotNull Event<@NotNull ShopEventArgs<@NotNull ?>> luckyChestEvent = new Event<>();

    public @NotNull Event<@NotNull ShopEventArgs<@NotNull ?>> getEvent(@NotNull String shopType) {
        return events.computeIfAbsent(shopType, unused -> new Event<>());
    }

    public @NotNull Event<@NotNull ShopEventArgs<@NotNull ?>> getLuckyChestEvent() {
        return luckyChestEvent;
    }

    @Override
    public void dispose() {

    }

}
