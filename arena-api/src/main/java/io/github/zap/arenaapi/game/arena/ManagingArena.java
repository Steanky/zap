package io.github.zap.arenaapi.game.arena;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.arenaapi.ResourceManager;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.MappingEvent;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.party.PartyPlusPlus;
import io.github.zap.party.party.PartyMember;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public abstract class ManagingArena<T extends ManagingArena<T, S>, S extends ManagedPlayer<S, T>> extends Arena<T>
implements Listener {
    @Value
    public static class PlayerListArgs {
        List<Player> players;
    }

    @Value
    public class ManagedPlayerListArgs {
        List<S> players;
    }

    /**
     * Wraps Bukkit events.
     * @param <U>
     */
    public class ProxyArgs<U extends org.bukkit.event.Event> {
        /**
         * The Bukkit event wrapped by this instance.
         */
        @Getter
        private final U event;

        /**
         * The managed players involved in this event.
         */
        private final List<S> managedPlayers;

        /**
         * The managed entities involved in this event.
         */
        private final List<UUID> managedEntities;

        public ProxyArgs(@NotNull U event, @NotNull List<S> managedPlayers, @NotNull List<UUID> managedEntities) {
            this.event = Objects.requireNonNull(event, "event cannot be null");
            this.managedPlayers = Objects.requireNonNull(managedPlayers, "managedPlayers cannot be null");
            this.managedEntities = Objects.requireNonNull(managedEntities, "managedEntities cannot be null");
        }

        /**
         * Returns the managed player associated with this event, assuming this event is a PlayerEvent
         * or PlayerDeathEvent. If this event is neither, this function will return null. This function will also
         * return null if there are multiple managed players associated with it, as in the case of an InventoryEvent.
         */
        public @Nullable S getManagedPlayer() {
            return managedPlayers.size() == 1 ? managedPlayers.get(0) : null;
        }

        /**
         * Gets the managed entity associated with this event. Will return null if this event is not an EntityEvent or
         * if this event is a PlayerDeathEvent (which shouldn't be an entityevent anyway). Will also return null if
         * this event has multiple managed entities associated with it.
         * @return The entity involved in this event
         */
        public @Nullable UUID getManagedEntity() {
            return managedEntities.size() == 1 ? managedEntities.get(0) : null;
        }
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
                    return Pair.of(true, new ProxyArgs<>(event, Lists.newArrayList(managedPlayer),
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
    private class AdaptedInventoryEvent<U extends InventoryEvent> extends MappingEvent<U, ProxyArgs<U>> {
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
                    return Pair.of(true, new ProxyArgs<>(event, managedViewers, new ArrayList<>()));
                }

                return Pair.of(false, null); //no ingame managed players are involved
            });
        }
    }

    private class AdaptedEntityEvent<U extends EntityEvent> extends MappingEvent<U, ProxyArgs<U>> {
        public AdaptedEntityEvent(Class<U> eventClass) {
            super(new ProxyEvent<>(plugin, eventClass, EventPriority.NORMAL, false), event -> {
                UUID entityUUID = event.getEntity().getUniqueId();

                if(entitySet.contains(entityUUID)) { //only managed entities trigger event
                    return Pair.of(true, new ProxyArgs<>(event, new ArrayList<>(), Lists.newArrayList(entityUUID)));
                }
                else if(playerMap.containsKey(entityUUID)) {
                    S player = playerMap.get(entityUUID);

                    if(player != null) {
                        return Pair.of(true, new ProxyArgs<>(event, Lists.newArrayList(player), new ArrayList<>()));
                    }
                }

                return Pair.of(false, null);
            });
        }
    }

    private class AdaptedPlayerDeathEvent extends MappingEvent<PlayerDeathEvent, ProxyArgs<PlayerDeathEvent>> {
        public AdaptedPlayerDeathEvent() {
            super(new ProxyEvent<>(plugin, PlayerDeathEvent.class, EventPriority.NORMAL, false), event -> {
                S managedPlayer = playerMap.get(event.getEntity().getUniqueId());

                if(managedPlayer != null && managedPlayer.isInGame()) {
                    return Pair.of(true, new ProxyArgs<>(event, Lists.newArrayList(managedPlayer), new ArrayList<>()));
                }

                return Pair.of(false, null);
            });
        }
    }

    private class AdaptedBlockEvent<U extends BlockEvent> extends MappingEvent<U, ProxyArgs<U>> {
        public AdaptedBlockEvent(Class<U> eventClass) {
            super(new ProxyEvent<>(plugin, eventClass, EventPriority.NORMAL, false), event -> {
                World world = event.getBlock().getWorld();

                if(world.equals(ManagingArena.this.world)) {
                    return Pair.of(true, new ProxyArgs<>(event, new ArrayList<>(), new ArrayList<>()));
                }

                return Pair.of(false, null);
            });
        }
    }

    private final Plugin plugin;
    private final ManagedPlayerBuilder<S, T> wrapper; //constructs instances of managed players
    private final long timeoutTicks;

    private BukkitTask timeoutTask;
    private boolean timingOut = false;

    private final ResourceManager resourceManager; //manages disposable resources

    private final Map<UUID, S> playerMap = new HashMap<>(); //holds managed player instances
    private final Set<UUID> entitySet = new HashSet<>(); //holds entities spawned over the course of this arena's life

    private int onlineCount;

    @SuppressWarnings("rawtypes")
    private final Map<Class<?>, Event> events = new HashMap<>(); //gross, but lets us very easily store proxyevents

    //custom events that aren't just wrapping Bukkit ones somehow
    private final Event<PlayerListArgs> playerJoinEvent = new Event<>();
    private final Event<ManagedPlayerListArgs> playerRejoinEvent = new Event<>();
    private final Event<ManagedPlayerListArgs> playerLeaveEvent = new Event<>();

    public ManagingArena(Plugin plugin, ArenaManager<T> manager, World world, ManagedPlayerBuilder<S, T> wrapper,
                         long timeoutTicks) {
        super(manager, world);
        this.plugin = plugin;
        this.wrapper = wrapper;
        this.timeoutTicks = timeoutTicks;
        this.resourceManager = new ResourceManager(plugin);

        startTimeout(timeoutTicks);
        getProxyFor(PlayerQuitEvent.class).registerHandler(this::onPlayerQuit);

        Bukkit.getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    /**
     * Basically just makes it so that players exiting the server while in an arena are treated as if they simply quit
     * the game.
     * @param args The PlayerQuitEvent and associated ManagedPlayer instance
     */
    private void onPlayerQuit(ProxyArgs<PlayerQuitEvent> args) {
        S managedPlayer = args.getManagedPlayer();

        if(managedPlayer != null) {
            handleLeave(Lists.newArrayList(managedPlayer.getPlayer()));
        }
    }

    /**
     * Disables sending messages to other worlds other than the one this arena manages
     * @param event The async chat even
     */
    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) { // public so that subclasses also register
        PartyPlusPlus partyPlusPlus = ArenaApi.getInstance().getPartyPlusPlus();
        if (partyPlusPlus != null) {
            PartyMember partyMember = partyPlusPlus.getPartyManager().getPlayerAsPartyMember(event.getPlayer());
            if (partyMember != null && partyMember.isInPartyChat()) {
                return;
            }
        }

        if (world.equals(event.getPlayer().getWorld())) {
            event.viewers().removeIf(audience -> !(audience instanceof Entity entity)
                    || !world.equals(entity.getWorld()));
            Component message = event.message();
            if (message instanceof TextComponent textComponent) {
                String string = textComponent.content();
                String lowerCase = string.toLowerCase();

                boolean isIAm = false;
                int startIndex = lowerCase.indexOf("i am ");

                if (startIndex != -1) {
                    startIndex += "i am ".length();
                    isIAm = true;
                } else {
                    startIndex = lowerCase.indexOf("i'm ");
                    if (startIndex != -1) {
                        startIndex += "i'm ".length();
                        isIAm = true;
                    }
                }
                if (isIAm) {
                    int endIndex = string.indexOf(" ", startIndex);
                    if (endIndex == -1) {
                        endIndex = string.length();
                    }
                    int finalStartIndex = startIndex, finalEndIndex = endIndex;
                    Bukkit.getScheduler().runTask(ArenaApi.getInstance(),
                            () -> event.getPlayer()
                                    .displayName(Component.text(string.substring(finalStartIndex, finalEndIndex))));
                }
            }
        } else {
            event.viewers().removeIf(audience -> !(audience instanceof Entity entity)
                    || world.equals(entity.getWorld()));
        }
    }

    private void startTimeout(long ticks) {
        if(!timingOut) {
            timeoutTask = new BukkitRunnable() {
                @Override
                public void run() {
                    dispose();
                }
            }.runTaskLater(plugin, ticks);
            timingOut = true;
        }
    }

    private void stopTimeout() {
        if(timingOut) {
            timeoutTask.cancel();
            timingOut = false;
        }
    }

    /**
     * Gets a proxy event of the specified type; creating it if necessary, and registering it with this ManagingArena's
     * resource manager.
     * @param bukkitEventClass The Bukkit event class to wrap. If the class type is not supported, an
     *                         IllegalArgumentException will be thrown
     * @param <U> The type of Bukkit event
     * @return The Bukkit event, wrapped in ProxyArgs
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U extends org.bukkit.event.Event> Event<ProxyArgs<U>> getProxyFor(Class<U> bukkitEventClass) {
        return events.computeIfAbsent(bukkitEventClass, bukkitClass -> {
            Event event;

            if(PlayerEvent.class.isAssignableFrom(bukkitClass)) {
                event = new AdaptedPlayerEvent(bukkitClass);
            }
            else if(PlayerDeathEvent.class.isAssignableFrom(bukkitClass)) {
                event = new AdaptedPlayerDeathEvent();
            }
            else if(InventoryEvent.class.isAssignableFrom(bukkitClass)) {
                event = new AdaptedInventoryEvent(bukkitClass);
            }
            else if(EntityEvent.class.isAssignableFrom(bukkitClass)) {
                event = new AdaptedEntityEvent(bukkitClass);
            }
            else if(BlockEvent.class.isAssignableFrom(bukkitClass)) {
                event = new AdaptedBlockEvent(bukkitClass);
            }
            else {
                throw new IllegalArgumentException("proxy events of type " + bukkitClass.getName() + " are not supported!");
            }

            resourceManager.addDisposable(event);
            return event;
        });
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
        player.dispose();
    }

    /**
     * Removes multiple players from this ManagingArena.
     * @param players The players to remove
     */
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

            if(onlineCount > 0) {
                stopTimeout();
            }

            // Hide/Show player logic
            for (Player player : joining) {
                for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                    S managedPlayer = playerMap.get(otherPlayer.getUniqueId());
                    if (managedPlayer == null || !managedPlayer.isInGame()) {
                        player.hidePlayer(ArenaApi.getInstance(), otherPlayer);
                        otherPlayer.hidePlayer(ArenaApi.getInstance(), player);
                    }
                    else {
                        player.showPlayer(ArenaApi.getInstance(), otherPlayer);
                        otherPlayer.showPlayer(ArenaApi.getInstance(), player);
                    }
                }
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
            }
        }

        onlineCount -= leftPlayers.size();

        if(leftPlayers.size() > 0) {
            playerLeaveEvent.callEvent(new ManagedPlayerListArgs(leftPlayers));
        }

        if(onlineCount == 0) {
            startTimeout(timeoutTicks);
        }

        for(S player : leftPlayers) {
            player.quit();
        }

        // Hide/Show player logic
        for (S player : playerMap.values()) {
            if (player.isInGame()) {
                Player bukkitPlayer = player.getPlayer();
                if (bukkitPlayer != null) {
                    for (Player leaver : leaving) {
                        bukkitPlayer.hidePlayer(ArenaApi.getInstance(), leaver);
                        leaver.hidePlayer(ArenaApi.getInstance(), bukkitPlayer);
                    }
                }
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Arena<?> arena = ArenaApi.getInstance().arenaCurrentlyIn(player);
            if (arena == null) {
                for (Player leaver : leaving) {
                    leaver.showPlayer(ArenaApi.getInstance(), player);
                }
            }
        }
    }

    @Override
    public boolean hasPlayer(UUID id) {
        return playerMap.containsKey(id);
    }

    @Override
    public boolean isPlayerPlaying(UUID id) {
        S player = playerMap.get(id);
        if (player == null) {
            return false;
        }

        return player.isInGame();
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
     * @param task The task to run
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
     * @param task The task to run
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


    /**
     * Schedules a new sync repeating task for this ManagingArena. The task will be cancelled automatically by the
     * ManagingArena when it is disposed, if necessary.
     * @param delay The initial delay
     * @param period The period (length between iterations)
     * @param task The task to run
     */
    public BukkitTask runTaskTimer(long delay, long period, DisposableBukkitRunnable task) {
        resourceManager.addDisposable(task);
        return task.runTaskTimer(plugin, delay, period);
    }

    /**
     * Schedules a new sync task, which will be automatically cancelled by this ManagingArena when it is disposed, if
     * necessary. It will run once.
     * @param delay The initial delay
     * @param task The task to run
     */
    public BukkitTask runTaskLater(long delay, DisposableBukkitRunnable task) {
        resourceManager.addDisposable(task);
        return task.runTaskLater(plugin, delay);
    }

    @Override
    public void dispose() {
        resourceManager.dispose(); //clear resources

        stopTimeout();

        for(S player : new ArrayList<>(playerMap.values())) { //dispose players
            player.dispose();
        }

        for(UUID entity : entitySet) { //remove entities
            Entity bukkitEntity = Bukkit.getEntity(entity);

            if(bukkitEntity != null) {
                bukkitEntity.remove();
            }
        }

        AsyncChatEvent.getHandlerList().unregister(this);
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
