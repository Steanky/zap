package io.github.zap.arenaapi.game.arena;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.MappingEvent;
import io.github.zap.arenaapi.event.ProxyEvent;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
public abstract class ManagingArena<T extends ManagingArena<T, S>, S extends ManagedPlayer<S, T>> extends Arena<T> {
    @Value
    public static class PlayerListArgs {
        List<Player> players;
    }

    @Value
    public class ManagedPlayerListArgs {
        List<S> players;
    }

    @Value
    public class ManagedInventoryEventArgs<U extends InventoryEvent> {
        /**
         * The original Bukkit event
         */
        U event;

        /**
         * The managed players involved in the event. This will always be non-null and size > 0
         */
        List<S> players;
    }


    @Value
    public class ProxyArgs<U extends org.bukkit.event.Event> {
        /**
         * The Bukkit event wrapped by this instance.
         */
        U event;

        /**
         * The managed player involved in the event. This will be null if the event is not a PlayerEvent (or
         * PlayerDeathEvent).
         */
        S managedPlayer;
    }

    @Value
    public static class ArenaEventArgs<T extends ManagingArena<T, S>, S extends ManagedPlayer<S, T>> {
        ManagingArena<T, S> arena;
    }

    /**
     * Wraps proxy events in an additional validation layer; they will only fire for online managed players.
     * Additionally, the event arguments will always consist of an EventProxyArguments instance containing the
     * managed player and the Bukkit event. For non-player events, the managed player will be null.
     * @param <U> The type of Bukkit event
     */
    private class AdaptedPlayerEvent<U extends PlayerEvent> extends MappingEvent<U, ProxyArgs<U>> {
        public AdaptedPlayerEvent(Class<U> bukkitEventClass) {
            super(new ProxyEvent<>(plugin, bukkitEventClass, EventPriority.NORMAL,
                    false), event -> {
                S managedPlayer = playerMap.get(event.getPlayer().getUniqueId());

                if(managedPlayer != null && managedPlayer.isInGame()) {
                    return ImmutablePair.of(true, new ProxyArgs<>(event, managedPlayer));
                }

                return ImmutablePair.of(false, null);
            });

            resourceManager.addDisposable(this);
        }
    }

    /**
     * Wraps inventory events in the same way as player events.
     * @param <U>
     */
    private class AdaptedInventoryEvent<U extends InventoryEvent> extends MappingEvent<U, ManagedInventoryEventArgs<U>> {
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
                    return ImmutablePair.of(true, new ManagedInventoryEventArgs<>(event, managedViewers));
                }

                return ImmutablePair.of(false, null); //no ingame managed players are involved
            });

            resourceManager.addDisposable(this);
        }
    }

    private class AdaptedEntityEvent<U extends EntityEvent> extends MappingEvent<U, ProxyArgs<U>> {
        public AdaptedEntityEvent(Class<U> eventClass) {
            super(new ProxyEvent<>(plugin, eventClass, EventPriority.NORMAL, false), event -> {
                UUID entityUUID = event.getEntity().getUniqueId();
                S managedPlayer = playerMap.get(entityUUID);

                if(managedPlayer != null) {
                    if(managedPlayer.isInGame()) {
                        return ImmutablePair.of(true, new ProxyArgs<>(event, managedPlayer));
                    }
                }
                else {
                    if(entitySet.contains(entityUUID)) { //only managed entities trigger
                        return ImmutablePair.of(true, new ProxyArgs<>(event, null));
                    }
                }

                return ImmutablePair.of(false, null);
            });

            resourceManager.addDisposable(this);
        }
    }

    private class AdaptedPlayerDeathEvent extends MappingEvent<PlayerDeathEvent, ProxyArgs<PlayerDeathEvent>> {
        public AdaptedPlayerDeathEvent() {
            super(new ProxyEvent<>(plugin, PlayerDeathEvent.class, EventPriority.NORMAL, false), event -> {
                S managedPlayer = playerMap.get(event.getEntity().getUniqueId());

                if(managedPlayer != null && managedPlayer.isInGame()) {
                    return ImmutablePair.of(true, new ProxyArgs<>(event, managedPlayer));
                }

                return ImmutablePair.of(false, null);
            });

            resourceManager.addDisposable(this);
        }
    }

    private final Plugin plugin;
    private final ManagedPlayerBuilder<S, T> wrapper; //constructs instances of managed players
    private final long timeoutTicks;

    private final BukkitRunnable timeoutRunnable;

    private final ResourceManager resourceManager; //manages disposable resources

    private final Map<UUID, S> playerMap = new HashMap<>(); //holds managed player instances
    private final Set<UUID> entitySet = new HashSet<>(); //holds entities spawned over the course of this arena's life

    private int onlineCount;

    //custom events that aren't just wrapping Bukkit ones somehow
    private final Event<PlayerListArgs> playerJoinEvent = new Event<>();
    private final Event<ManagedPlayerListArgs> playerRejoinEvent = new Event<>();
    private final Event<ManagedPlayerListArgs> playerLeaveEvent = new Event<>();

    //bukkit events concerning players, but passed through our custom API and filtered to only fire for managed players
    //more will be added as needed
    private final Event<ProxyArgs<PlayerInteractEvent>> playerInteractEvent;
    private final Event<ProxyArgs<PlayerInteractAtEntityEvent>> playerInteractAtEntityEvent;
    private final Event<ProxyArgs<PlayerDropItemEvent>> playerDropItemEvent;
    private final Event<ProxyArgs<PlayerMoveEvent>> playerMoveEvent;
    private final Event<ProxyArgs<PlayerSwapHandItemsEvent>> playerSwapHandItemsEvent;
    private final Event<ProxyArgs<PlayerAnimationEvent>> playerAnimationEvent;
    private final Event<ProxyArgs<PlayerToggleSneakEvent>> playerToggleSneakEvent;
    private final Event<ProxyArgs<EntityDamageEvent>> playerDamagedEvent;
    private final Event<ProxyArgs<EntityDamageByEntityEvent>> entityDamageByEntityEvent;
    private final Event<ProxyArgs<PlayerDeathEvent>> playerDeathEvent;
    private final Event<ProxyArgs<PlayerQuitEvent>> playerQuitEvent;
    private final Event<ProxyArgs<PlayerItemHeldEvent>> playerItemHeldEvent;
    private final Event<ProxyArgs<PlayerItemConsumeEvent>> playerItemConsumeEvent;
    private final Event<ProxyArgs<PlayerItemDamageEvent>> playerItemDamageEvent;
    private final Event<ProxyArgs<PlayerAttemptPickupItemEvent>> playerAttemptPickupItemEvent;
    private final Event<ProxyArgs<PlayerArmorStandManipulateEvent>> playerArmorStandManipulateEvent;
    private final Event<ProxyArgs<FoodLevelChangeEvent>> playerFoodLevelChangeEvent;

    private final Event<ManagedInventoryEventArgs<InventoryOpenEvent>> inventoryOpenEvent;
    private final Event<ManagedInventoryEventArgs<InventoryClickEvent>> inventoryClickEvent;

    public ManagingArena(Plugin plugin, ArenaManager<T> manager, World world, ManagedPlayerBuilder<S, T> wrapper,
                         long timeoutTicks) {
        super(manager, world);
        this.plugin = plugin;
        this.wrapper = wrapper;
        this.timeoutTicks = timeoutTicks;

        timeoutRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                dispose();
            }
        };
        timeoutRunnable.runTaskLater(plugin, timeoutTicks);

        this.resourceManager = new ResourceManager(plugin);

        playerInteractEvent = new AdaptedPlayerEvent<>(PlayerInteractEvent.class);
        playerInteractAtEntityEvent = new AdaptedPlayerEvent<>(PlayerInteractAtEntityEvent.class);
        playerDropItemEvent = new AdaptedPlayerEvent<>(PlayerDropItemEvent.class);
        playerMoveEvent = new AdaptedPlayerEvent<>(PlayerMoveEvent.class);
        playerSwapHandItemsEvent = new AdaptedPlayerEvent<>(PlayerSwapHandItemsEvent.class);
        playerAnimationEvent = new AdaptedPlayerEvent<>(PlayerAnimationEvent.class);
        playerToggleSneakEvent = new AdaptedPlayerEvent<>(PlayerToggleSneakEvent.class);
        playerDamagedEvent = new AdaptedEntityEvent<>(EntityDamageEvent.class);
        playerDeathEvent = new AdaptedPlayerDeathEvent();
        playerQuitEvent = new AdaptedPlayerEvent<>(PlayerQuitEvent.class);
        playerItemHeldEvent = new AdaptedPlayerEvent<>(PlayerItemHeldEvent.class);
        playerItemConsumeEvent = new AdaptedPlayerEvent<>(PlayerItemConsumeEvent.class);
        playerItemDamageEvent = new AdaptedPlayerEvent<>(PlayerItemDamageEvent.class);
        playerAttemptPickupItemEvent = new AdaptedPlayerEvent<>(PlayerAttemptPickupItemEvent.class);
        playerArmorStandManipulateEvent = new AdaptedPlayerEvent<>(PlayerArmorStandManipulateEvent.class);
        playerFoodLevelChangeEvent = new AdaptedEntityEvent<>(FoodLevelChangeEvent.class);

        entityDamageByEntityEvent = new AdaptedEntityEvent<>(EntityDamageByEntityEvent.class);

        inventoryOpenEvent = new AdaptedInventoryEvent<>(InventoryOpenEvent.class);
        inventoryClickEvent = new AdaptedInventoryEvent<>(InventoryClickEvent.class);

        playerQuitEvent.registerHandler(this::onPlayerQuit);
    }

    /**
     * Basically just makes it so that players exiting the server while in an arena are treated as if they simply quit
     * the game.
     * @param args The PlayerQuitEvent and associated ManagedPlayer instance
     */
    private void onPlayerQuit(ProxyArgs<PlayerQuitEvent> args) {
        handleLeave(Lists.newArrayList(args.managedPlayer.getPlayer()));
    }

    /**
     * Removes a managed player from the internal map, if they exist. They will no longer be managed by this arena.
     * @param id The UUID of the managed player
     */
    public void removePlayer(UUID id) {
        S managedPlayer = playerMap.remove(id);

        if(managedPlayer != null) {
            managedPlayer.dispose();
        }
    }

    /**
     * Removes a managed player from the internal map, if they exist. They will no longer be managed by this arena.
     * @param player The player to remove
     */
    public void removePlayer(S player) {
        removePlayer(player.getId());
    }

    public void removePlayers(Iterable<S> players) {
        for(S player : players) {
            removePlayer(player);
        }
    }

    @Override
    public boolean handleJoin(List<Player> joining) {
        if(allowPlayers()) { //arena can deny all join requests
            List<Player> newPlayers = new ArrayList<>();
            List<S> rejoiningPlayers = new ArrayList<>();

            for(Player player : joining) { //sort joining players
                UUID id = player.getUniqueId();
                S managedPlayer = playerMap.get(id);

                if(managedPlayer != null) {
                    if(!managedPlayer.isInGame()) { //don't rejoin redundantly
                        rejoiningPlayers.add(managedPlayer);
                    }
                }
                else {
                    newPlayers.add(player);
                }
            }

            //perform checks on new/rejoining players
            if(newPlayers.size() > 0 && !allowPlayerJoin(newPlayers)) {
                return false;
            }

            if(rejoiningPlayers.size() > 0 && !allowPlayerRejoin(rejoiningPlayers)) {
                return false;
            }

            //if both succeeded, update onlineCount
            onlineCount += newPlayers.size() + rejoiningPlayers.size();

            if(newPlayers.size() > 0) { //wrap players, call event
                T arena = getArena();
                for(Player player : newPlayers) {
                    playerMap.put(player.getUniqueId(), wrapper.wrapPlayer(arena, player));
                }

                playerJoinEvent.callEvent(new PlayerListArgs(newPlayers));
            }

            if(rejoiningPlayers.size() > 0) { //rejoin players, call event
                for(S rejoiningPlayer : rejoiningPlayers) {
                    rejoiningPlayer.rejoin();
                }

                playerRejoinEvent.callEvent(new ManagedPlayerListArgs(rejoiningPlayers));
            }

            return true;
        }

        return false;
    }

    @Override
    public void handleLeave(List<Player> leaving) {
        List<S> leftPlayers = new ArrayList<>();
        for(Player player : leaving) {
            S managedPlayer = playerMap.get(player.getUniqueId());

            if(managedPlayer != null && managedPlayer.isInGame()) {
                leftPlayers.add(managedPlayer);
                managedPlayer.quit();
            }
        }

        onlineCount -= leftPlayers.size();

        if(leftPlayers.size() > 0) {
            playerLeaveEvent.callEvent(new ManagedPlayerListArgs(leftPlayers));
        }

        if(playerMap.size() == 0 && timeoutRunnable.isCancelled()) {
            timeoutRunnable.runTaskLater(plugin, timeoutTicks);
        }
    }

    @Override
    public boolean hasPlayer(UUID id) {
        return playerMap.containsKey(id) && playerMap.get(id).isInGame();
    }

    /**
     * Returns whether or not this ManagingArena contains the specific entity.
     * @param entity The entity UUID
     * @return Whether or not this entity exists
     */
    public boolean hasEntity(UUID entity) {
        return entitySet.contains(entity);
    }

    /**
     * Schedules a new sync repeating task for this ManagingArena. The task will be cancelled automatically by the
     * ManagingArena when it is disposed, if necessary.
     * @param delay The initial delay
     * @param period The period (length between iterations)
     * @param task The Runnable to run
     */
    public BukkitTask runTaskTimer(long delay, long period, Runnable task) {
        DisposableBukkitRunnable runnable = new DisposableBukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        resourceManager.addDisposable(runnable);
        return runnable.runTaskTimer(plugin, delay, period);
    }

    /**
     * Schedules a new sync task, which will be automatically cancelled by this ManagingArena when it is disposed, if
     * necessary. It will run once.
     * @param delay The initial delay
     * @param task The task torun
     */
    public BukkitTask runTaskLater(long delay, Runnable task) {
        DisposableBukkitRunnable runnable = new DisposableBukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };

        resourceManager.addDisposable(runnable);
        return runnable.runTaskLater(plugin, delay);
    }

    @Override
    public void dispose() {
        resourceManager.dispose(); //clear resources

        for(S player : playerMap.values()) { //dispose players
            player.dispose();
        }

        for(UUID entity : entitySet) { //remove entities
            Entity bukkitEntity = Bukkit.getEntity(entity);

            if(bukkitEntity != null) {
                bukkitEntity.remove();
            }
        }
    }

    protected void disposeAfter(int ticks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                dispose();
            }
        }.runTaskLater(plugin, ticks);
    }

    /**
     * Returns true if this arena is in a state to allow players to join or rejoin the game. Returns false otherwise.
     * @return True if joining/rejoining is allowed, false otherwise
     */
    public abstract boolean allowPlayers();

    /**
     * Returns true if the provided list of new players should ALL be allowed to join the game.
     * @param players The players that are trying to join
     * @return True if they can join, false otherwise
     */
    public abstract boolean allowPlayerJoin(List<Player> players);

    /**
     * Returns true if the provided list of players should be able to rejoin the game.
     * @param players The players that are trying to rejoin
     * @return True if they can rejoin, false otherwise
     */
    public abstract boolean allowPlayerRejoin(List<S> players);

    /**
     * Helper method; gets the implementing arena
     * @return The implementing arena
     */
    public abstract T getArena();
}
