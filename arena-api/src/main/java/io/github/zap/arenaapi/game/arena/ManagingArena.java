package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.Joinable;
import lombok.Getter;
import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

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
        filterPlayerEvent(event, playerInteractEvent);
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        filterPlayerEvent(event, playerInteractAtEntityEvent);
    }

    @EventHandler
    private void onPlayerSneak(PlayerToggleSneakEvent event) {
        filterPlayerEvent(event, playerSneakEvent);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        filterEntityEvent(event, playerDeathEvent);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        S managedPlayer = managedPlayerMap.get(event.getPlayer().getUniqueId());

        if(validatePlayer(managedPlayer)) {
            //TODO: construct default LeaveInformation and call handleLeave()
        }
    }

    private <U extends EntityEvent> void filterEntityEvent(U bukkit, Event<BukkitProxyArgs<U>> arenaapi) {
        S managedPlayer = managedPlayerMap.get(bukkit.getEntity().getUniqueId());

        if(validatePlayer(managedPlayer)) {
            arenaapi.callEvent(new BukkitProxyArgs<>(bukkit, managedPlayer));
        }
    }

    private <U extends PlayerEvent> void filterPlayerEvent(U bukkit, Event<BukkitProxyArgs<U>> arenaapi) {
        S managedPlayer = managedPlayerMap.get(bukkit.getPlayer().getUniqueId());

        if(validatePlayer(managedPlayer)) {
            arenaapi.callEvent(new BukkitProxyArgs<>(bukkit, managedPlayer));
        }
    }

    private boolean validatePlayer(S player) {
        return player != null && player.isInGame();
    }

    @Override
    public boolean handleJoin(JoinInformation joinAttempt) {
        if(joinAllowed(joinAttempt)) {
            List<S> joiningPlayers = new ArrayList<>();

            for(Player player : joinAttempt.getJoinable().getPlayers()) {
                UUID uuid = player.getUniqueId();
                S managedPlayer = managedPlayerMap.get(uuid);

                if(managedPlayer != null) {
                    managedPlayer.rejoin();
                }
                else {
                    managedPlayer = playerWrapper.wrapPlayer(getArena(), player);
                    managedPlayerMap.put(uuid, managedPlayer);
                }

                joiningPlayers.add(managedPlayer);
            }

            if(joiningPlayers.size() > 0) {
                playerJoinEvent.callEvent(new PlayerListArgs(joiningPlayers));
            }

            return true;
        }

        return false;
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
        //close players
        for(S player : managedPlayerMap.values()) {
            player.close();
        }

        manager.removeArena(getArena());

        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerToggleSneakEvent.getHandlerList().unregister(this);
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    public abstract boolean joinAllowed(JoinInformation attempt);

    protected abstract T getArena();
}