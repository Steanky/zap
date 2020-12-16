package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.event.Event;
import lombok.Getter;
import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public abstract class ManagingArena<T extends ManagingArena<T, S>, S extends ManagedPlayer<S, T>> extends Arena<T>
        implements Listener {
    @Value
    public class PlayerListArgs {
        List<S> players;
    }

    @Value
    public class BukkitProxyArgs<U extends org.bukkit.event.Event> {
        U event;
        S player;
    }

    protected final Plugin plugin;

    protected final Event<PlayerListArgs> playerJoinEvent = new Event<>();
    protected final Event<PlayerListArgs> playerLeaveEvent = new Event<>();

    protected final Event<BukkitProxyArgs<PlayerInteractEvent>> playerInteractEvent = new Event<>();
    protected final Event<BukkitProxyArgs<PlayerInteractAtEntityEvent>> playerInteractAtEntityEvent = new Event<>();
    protected final Event<BukkitProxyArgs<PlayerToggleSneakEvent>> playerSneakEvent = new Event<>();
    protected final Event<BukkitProxyArgs<PlayerDeathEvent>> playerDeathEvent = new Event<>();

    @Getter
    private final Map<UUID, S> managedPlayerMap = new HashMap<>();

    private final ManagedPlayerBuilder<S, T> playerWrapper;

    public ManagingArena(Plugin plugin, ArenaManager<T> manager, World world, ManagedPlayerBuilder<S, T> wrapper) {
        super(manager, world);

        this.plugin = plugin;
        playerWrapper = wrapper;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        proxyEvent(event, event.getPlayer(), playerInteractEvent, (managed) -> managed.onPlayerInteract(event));
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        proxyEvent(event, event.getPlayer(), playerInteractAtEntityEvent, (managed) ->
                managed.onPlayerInteractAtEntity(event));
    }

    @EventHandler
    private void onPlayerSneak(PlayerToggleSneakEvent event) {
        proxyEvent(event, event.getPlayer(), playerSneakEvent, (managed) -> managed.onPlayerSneak(event));
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        proxyEvent(event, event.getEntity(), playerDeathEvent, (managed) -> managed.onPlayerDeath(event));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {

    }

    private S getManagedIfValid(Player player) {
        S managedPlayer = managedPlayerMap.get(player.getUniqueId());
        return managedPlayer != null ? (managedPlayer.isInGame() ? managedPlayer : null) : null;
    }

    private <U extends org.bukkit.event.Event> void proxyEvent(U event, Player player,
                                                               Event<BukkitProxyArgs<U>> proxyEvent,
                                                               Consumer<S> postCall) {
        S managedPlayer = getManagedIfValid(player);

        if(managedPlayer != null) {
            proxyEvent.callEvent(new BukkitProxyArgs<>(event, managedPlayer));

            if(postCall != null) {
                postCall.accept(managedPlayer);
            }
        }
    }

    @Override
    public boolean handleJoin(JoinInformation joinAttempt) {
        List<S> joiningPlayers = new ArrayList<>();

        for(Player player : joinAttempt.getJoinable().getPlayers()) {
            UUID uuid = player.getUniqueId();
            S managedPlayer = managedPlayerMap.get(uuid);

            if(managedPlayer != null) {
                if(canAcceptExisting(managedPlayer)) {
                    managedPlayer.rejoin();
                }
                else {
                    return false;
                }
            }
            else {
                if(canAcceptNew(player)) {
                    managedPlayer = playerWrapper.wrapPlayer(getArena(), player);
                    managedPlayerMap.put(uuid, managedPlayer);
                }
                else {
                    return false;
                }
            }

            joiningPlayers.add(managedPlayer);
        }

        if(joiningPlayers.size() > 0) {
            playerJoinEvent.callEvent(new PlayerListArgs(joiningPlayers));
        }

        return true;
    }

    @Override
    public void handleLeave(LeaveInformation leaveInformation) {
        List<S> leavingPlayers = new ArrayList<>();

        for(Player player : leaveInformation.getJoinable().getPlayers()) {
            S managedPlayer = managedPlayerMap.get(player.getUniqueId());

            if(managedPlayer != null) {
                managedPlayer.quit();
                leavingPlayers.add(managedPlayer);
            }
        }

        if(leavingPlayers.size() > 0) {
            playerLeaveEvent.callEvent(new PlayerListArgs(leavingPlayers));
        }
    }

    @Override
    public void close() {
        for(S player : managedPlayerMap.values()) { //close players
            player.close();
        }

        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        PlayerToggleSneakEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);

        manager.removeArena(getArena());
    }

    public abstract boolean canAcceptNew(Player player);

    public abstract boolean canAcceptExisting(S player);

    protected abstract T getArena();
}