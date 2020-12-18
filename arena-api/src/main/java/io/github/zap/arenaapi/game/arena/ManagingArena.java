package io.github.zap.arenaapi.game.arena;

import com.google.common.collect.Lists;
import io.github.zap.arenaapi.event.ProxyEvent;
import io.github.zap.arenaapi.event.Event;
import lombok.Getter;
import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

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
     * Used internally to route events
     * @param <U> The type of Bukkit event
     */
    private class FilteredEvent<U extends org.bukkit.event.Event> extends ProxyEvent<U> {
        public FilteredEvent(Class<U> bukkitEventClass) {
            super(plugin, ManagingArena.this, ManagingArena.this::validateEvent, bukkitEventClass,
                    EventPriority.NORMAL, true);
        }
    }

    protected final Event<PlayerListArgs> playerJoinEvent = new Event<>();
    protected final Event<ManagedPlayerListArgs> playerRejoinEvent = new Event<>();
    protected final Event<ManagedPlayerListArgs> playerLeaveEvent = new Event<>();

    //bukkit events concerning players, but passed through our custom API and filtered to only fire for managed players
    protected final Event<PlayerInteractEvent> playerInteractEvent = new FilteredEvent<>(PlayerInteractEvent.class);
    protected final Event<PlayerInteractAtEntityEvent> playerInteractAtEntityEvent = new FilteredEvent<>(PlayerInteractAtEntityEvent.class);
    protected final Event<PlayerToggleSneakEvent> playerToggleSneakEvent = new FilteredEvent<>(PlayerToggleSneakEvent.class);
    protected final Event<PlayerDeathEvent> playerDeathEvent = new FilteredEvent<>(PlayerDeathEvent.class);

    private final Plugin plugin;
    private final ManagedPlayerBuilder<S, T> wrapper; //constructs instances of managed players

    private final Map<UUID, S> playerMap = new HashMap<>(); //holds managed player instances

    @Getter
    private int onlineCount;

    public ManagingArena(Plugin plugin, ArenaManager<T> manager, World world, ManagedPlayerBuilder<S, T> wrapper) {
        super(manager, world);
        this.plugin = plugin;
        this.wrapper = wrapper;

        //quit events are fully handled by this class
        Event<PlayerQuitEvent> playerQuitEvent = new FilteredEvent<>(PlayerQuitEvent.class);
        playerQuitEvent.registerHandler(this::onPlayerQuit);
    }

    private boolean validateEvent(org.bukkit.event.Event event) {
        UUID uuid = null;
        if(event instanceof PlayerEvent) {
            uuid = ((PlayerEvent) event).getPlayer().getUniqueId();
        }
        else if(event instanceof PlayerDeathEvent) {
            uuid = ((PlayerDeathEvent) event).getEntity().getUniqueId();
        }

        if(uuid != null) {
            S managedPlayer = playerMap.get(uuid);
            return managedPlayer != null && managedPlayer.isInGame();
        }

        return true;
    }

    private void onPlayerQuit(Event<PlayerQuitEvent> caller, PlayerQuitEvent args) {
        handleLeave(Lists.newArrayList(args.getPlayer()));
    }

    /**
     * Removes a managed player from the internal map, if they exist.
     * @param id The UUID of the managed player
     */
    public void removePlayer(UUID id) {
        S managedPlayer = playerMap.remove(id);

        if(managedPlayer != null) {
            managedPlayer.close();
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
    }

    @Override
    public void close() {
        for(S player : playerMap.values()) { //close players
            player.close();
        }

        ProxyEvent.closeAll(this); //closes proxy events
    }

    /**
     * Returns true if this arena is in a state to allow players to join or rejoin the game. Returns false otherwise.
     * @return True if joining/rejoining is allowed, false otherwise
     */
    protected abstract boolean allowPlayers();

    /**
     * Returns true if the provided list of new players should ALL be allowed to join the game.
     * @param players The players that are trying to join
     * @return True if they can join, false otherwise
     */
    protected abstract boolean allowPlayerJoin(List<Player> players);

    /**
     * Returns true if the provided list of players should be able to rejoin the game.
     * @param players The players that are trying to rejoin
     * @return True if they can rejoin, false otherwise
     */
    protected abstract boolean allowPlayerRejoin(List<S> players);

    /**
     * Helper method; gets the implementing arena
     * @return The implementing arena
     */
    protected abstract T getArena();
}