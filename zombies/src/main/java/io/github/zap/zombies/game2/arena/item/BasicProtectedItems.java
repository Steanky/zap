package io.github.zap.zombies.game2.arena.item;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BasicProtectedItems implements ProtectedItems, Listener {

    private final Set<Item> protectedItems = new HashSet<>();

    @Override
    public void addItem(@NotNull Item item) {
        protectedItems.add(item);
    }

    @Override
    public void removeItem(@NotNull Item item) {
        protectedItems.remove(item);
    }

    @EventHandler
    public void onItemDespawn(@NotNull ItemDespawnEvent event) {
        if (protectedItems.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

}
