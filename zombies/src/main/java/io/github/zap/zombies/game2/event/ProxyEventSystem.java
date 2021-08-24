package io.github.zap.zombies.game2.event;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.MappingEvent;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProxyEventSystem {

    private final Map<Class<?>, Event<? extends org.bukkit.event.Event>> events = new IdentityHashMap<>();

    public <E extends org.bukkit.event.Event> Event<E> getProxyFor(@NotNull Class<E> bukkitEventClass) {
        return (Event<E>) events.computeIfAbsent(bukkitEventClass, clazz -> {
        });
    }

    private class AdaptedPlayerEvent<U extends PlayerEvent> extends MappingEvent<U, U> {
        public AdaptedPlayerEvent(@NotNull Class<U> bukkitEventClass) {
            super(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.HIGHEST, false), event -> {
                S managedPlayer = playerMap.get(event.getPlayer().getUniqueId());

                if(managedPlayer != null && managedPlayer.isInGame()) {
                    return Pair.of(true, new ManagingArena.ProxyArgs<>(event, Lists.newArrayList(managedPlayer),
                            new ArrayList<>()));
                }

                return Pair.of(false, null);
            });
        }
    }

    /**
     * Wraps inventory events in the same way as player events.
     * @param <U>
     */
    private class AdaptedInventoryEvent<U extends InventoryEvent> extends MappingEvent<U, ManagingArena.ProxyArgs<U>> {
        public AdaptedInventoryEvent(Class<U> bukkitEventClass) {
            super(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
                List<HumanEntity> viewers = event.getViewers();
                List<S> managedViewers = new ArrayList<>();

                for(HumanEntity human : viewers) {
                    S managedViewer = playerMap.get(human.getUniqueId());

                    if(managedViewer != null && managedViewer.isInGame()) {
                        managedViewers.add(managedViewer);
                    }
                }

                if(managedViewers.size() > 0) {
                    return Pair.of(true, new ManagingArena.ProxyArgs<>(event, managedViewers, new ArrayList<>()));
                }

                return Pair.of(false, null); //no ingame managed players are involved
            });
        }
    }

    private class AdaptedEntityEvent<U extends EntityEvent> extends MappingEvent<U, ManagingArena.ProxyArgs<U>> {
        public AdaptedEntityEvent(Class<U> eventClass) {
            super(new ProxyEvent<>(plugin, eventClass, EventPriority.NORMAL, false), event -> {
                UUID entityUUID = event.getEntity().getUniqueId();

                if(entitySet.contains(entityUUID)) { //only managed entities trigger event
                    return Pair.of(true, new ManagingArena.ProxyArgs<>(event, new ArrayList<>(), Lists.newArrayList(entityUUID)));
                }
                else if(playerMap.containsKey(entityUUID)) {
                    S player = playerMap.get(entityUUID);

                    if(player != null) {
                        return Pair.of(true, new ManagingArena.ProxyArgs<>(event, Lists.newArrayList(player), new ArrayList<>()));
                    }
                }

                return Pair.of(false, null);
            });
        }
    }

    private class AdaptedPlayerDeathEvent extends MappingEvent<PlayerDeathEvent, ManagingArena.ProxyArgs<PlayerDeathEvent>> {
        public AdaptedPlayerDeathEvent() {
            super(new ProxyEvent<>(plugin, PlayerDeathEvent.class, EventPriority.NORMAL, false), event -> {
                S managedPlayer = playerMap.get(event.getEntity().getUniqueId());

                if(managedPlayer != null && managedPlayer.isInGame()) {
                    return Pair.of(true, new ManagingArena.ProxyArgs<>(event, Lists.newArrayList(managedPlayer), new ArrayList<>()));
                }

                return Pair.of(false, null);
            });
        }
    }

    private class AdaptedBlockEvent<U extends BlockEvent> extends MappingEvent<U, ManagingArena.ProxyArgs<U>> {
        public AdaptedBlockEvent(Class<U> eventClass) {
            super(new ProxyEvent<>(plugin, eventClass, EventPriority.NORMAL, false), event -> {
                World world = event.getBlock().getWorld();

                if(world.equals(ManagingArena.this.world)) {
                    return Pair.of(true, new ManagingArena.ProxyArgs<>(event, new ArrayList<>(), new ArrayList<>()));
                }

                return Pair.of(false, null);
            });
        }
    }

}
