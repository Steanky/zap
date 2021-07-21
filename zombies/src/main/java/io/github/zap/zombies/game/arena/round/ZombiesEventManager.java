package io.github.zap.zombies.game.arena.round;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.MappingEvent;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.game.arena.event.EntityArgs;
import io.github.zap.arenaapi.game.arena.event.EventManager;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.game.arena.player.PlayerList;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.tuple.Pair;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages events for Zombies
 */
public class ZombiesEventManager implements EventManager {

    private final Plugin plugin;

    private final World world;

    private final PlayerList<ZombiesPlayer> zombiesPlayerList;

    private final Set<Mob> mobs;

    private final Event<@NotNull List<@NotNull Player>> playerJoinEvent = new Event<>();

    private final Event<@NotNull List<@NotNull ZombiesPlayer>> playerRejoinEvent = new Event<>();

    private final Event<@NotNull List<@NotNull ZombiesPlayer>> playerLeaveEvent = new Event<>();

    @SuppressWarnings("rawtypes")
    private final Map<@NotNull Class<?>, Event> managedProxies = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private final Map<@NotNull Class<?>, Event> mobProxies = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private final Map<@NotNull Class<?>, Event> proxies = new HashMap<>();

    public ZombiesEventManager(@NotNull Plugin plugin, @NotNull World world,
                               @NotNull PlayerList<ZombiesPlayer> zombiesPlayerList, @NotNull Set<Mob> mobs) {
        this.plugin = plugin;
        this.world = world;
        this.zombiesPlayerList = zombiesPlayerList;
        this.mobs = mobs;
    }

    public @NotNull Event<@NotNull List<@NotNull Player>> getPlayerJoinEvent() {
        return playerJoinEvent;
    }

    public @NotNull Event<@NotNull List<@NotNull ZombiesPlayer>> getPlayerRejoinEvent() {
        return playerRejoinEvent;
    }

    public @NotNull Event<@NotNull List<@NotNull ZombiesPlayer>> getPlayerLeaveEvent() {
        return playerLeaveEvent;
    }

    /**
     * Gets the proxy for {@link PlayerEvent}s that are triggered by {@link ZombiesPlayer}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @SuppressWarnings("unchecked")
    public @NotNull <E extends PlayerEvent> Event<@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull E>> getZombiesPlayerProxy(@NotNull Class<E> bukkitEventClass) {
        return managedProxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            ZombiesPlayer player = zombiesPlayerList.getPlayer(event.getPlayer());
            if (player != null && player.isInGame()) {
                return Pair.of(true, new ManagedPlayerArgs<>(player, event));
            }

            return Pair.of(false, null);
        }));
    }

    /**
     * Gets the proxy for {@link PlayerDeathEvent}s that are triggered by {@link ZombiesPlayer}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @SuppressWarnings("unchecked")
    public @NotNull <E extends PlayerDeathEvent> Event<@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull E>> getZombiesPlayerDeathProxy(@NotNull Class<E> bukkitEventClass) {
        return managedProxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            ZombiesPlayer player = zombiesPlayerList.getPlayer(event.getEntity());
            if (player != null && player.isInGame()) {
                return Pair.of(true, new ManagedPlayerArgs<>(player, event));
            }

            return Pair.of(false, null);
        }));
    }

    /**
     * Gets the proxy for {@link EntityEvent}s that are triggered by {@link ZombiesPlayer}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @SuppressWarnings("unchecked")
    public @NotNull <E extends EntityEvent> Event<@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull E>> getZombiesPlayerEntityProxy(@NotNull Class<E> bukkitEventClass) {
        return managedProxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            ZombiesPlayer player = zombiesPlayerList.getPlayer(event.getEntity().getUniqueId());
            if (player != null && player.isInGame()) {
                return Pair.of(true, new ManagedPlayerArgs<>(player, event));
            }

            return Pair.of(false, null);
        }));
    }

    /**
     * Gets the proxy for {@link InventoryEvent}s that are triggered by {@link ZombiesPlayer}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @SuppressWarnings("unchecked")
    public @NotNull <E extends InventoryEvent> Event<@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, @NotNull E>> getZombiesPlayerInventoryProxy(@NotNull Class<E> bukkitEventClass) {
        return managedProxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            List<ZombiesPlayer> players = new ArrayList<>();

            for (HumanEntity entity : event.getViewers()) {
                ZombiesPlayer player = zombiesPlayerList.getPlayer(entity.getUniqueId());
                if (player != null && player.isInGame()) {
                    players.add(player);
                }
            }

            if (players.isEmpty()) {
                return Pair.of(false, null);
            } else {
                return Pair.of(true, Collections.unmodifiableList(players));
            }
        }));
    }

    /**
     * Gets the proxy for {@link EntityEvent}s that are triggered by managed {@link Mob}s
     * @param bukkitEventClass The class of the event
     * @param <E> The type of the event
     * @return The proxy event
     */
    @SuppressWarnings("unchecked")
    public @NotNull <E extends EntityEvent> Event<@NotNull EntityArgs<@NotNull Mob, @NotNull E>> getMobProxy(@NotNull Class<E> bukkitEventClass) {
        return mobProxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            if (event.getEntity() instanceof Mob mob && mobs.contains(mob)) {
                return Pair.of(true, new EntityArgs<>(mob, event));
            }

            return Pair.of(false, null);
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <E extends PlayerEvent> Event<@NotNull E> getPlayerProxy(@NotNull Class<E> bukkitEventClass) {
        return proxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            if (event.getPlayer().getWorld().equals(world)) {
                return Pair.of(true, event);
            }

            return Pair.of(false, null);
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <E extends EntityEvent> Event<@NotNull E> getEntityProxy(@NotNull Class<E> bukkitEventClass) {
        return proxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            if (event.getEntity().getWorld().equals(world)) {
                return Pair.of(true, event);
            }

            return Pair.of(false, null);
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <E extends BlockEvent> Event<@NotNull E> getBlockProxy(@NotNull Class<E> bukkitEventClass) {
        return proxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> {
            if (event.getBlock().getWorld().equals(world)) {
                return Pair.of(true, event);
            }

            return Pair.of(false, null);
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <E extends InventoryEvent> Event<@NotNull E> getInventoryProxy(@NotNull Class<E> bukkitEventClass) {
        return proxies.computeIfAbsent(bukkitEventClass, (clazz) -> new MappingEvent<>(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL, false), event -> Pair.of(true, event)));
    }

}
