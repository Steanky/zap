package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.event.BukkitProxyEvent;
import io.github.zap.arenaapi.event.Event;
import lombok.Getter;
import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public abstract class ManagingArena<T extends ManagingArena<T, S>, S extends ManagedPlayer<S, T>> extends Arena<T>
        implements Listener {
    @Value
    public class PlayerListArgs {
        List<S> players;
    }

    protected final Plugin plugin;

    protected final Event<PlayerListArgs> playerJoinEvent = new Event<>();
    protected final Event<PlayerListArgs> playerLeaveEvent = new Event<>();

    //bukkit events concerning players, but passed through our custom API and filtered to only fire for managed players
    protected final Event<PlayerInteractEvent> playerInteractEvent;
    protected final Event<PlayerInteractAtEntityEvent> playerInteractAtEntityEvent;
    protected final Event<PlayerToggleSneakEvent> playerToggleSneakEvent;
    protected final Event<PlayerDeathEvent> playerDeathEvent;

    @Getter
    private final Map<UUID, S> managedPlayerMap = new HashMap<>();
    private final ManagedPlayerBuilder<S, T> wrapper;

    public ManagingArena(Plugin plugin, ArenaManager<T> manager, World world, ManagedPlayerBuilder<S, T> wrapper) {
        super(manager, world);
        this.plugin = plugin;
        this.wrapper = wrapper;

        playerInteractEvent = buildProxyEvent(PlayerInteractEvent.class);
        playerInteractAtEntityEvent = buildProxyEvent(PlayerInteractAtEntityEvent.class);
        playerToggleSneakEvent = buildProxyEvent(PlayerToggleSneakEvent.class);
        playerDeathEvent = new BukkitProxyEvent<>(plugin, event -> validateUUID(event.getEntity().getUniqueId()),
                PlayerDeathEvent.class);
    }

    private <U extends PlayerEvent> BukkitProxyEvent<U> buildProxyEvent(Class<U> eventClass) {
        return new BukkitProxyEvent<>(plugin, event -> validateUUID(event.getPlayer().getUniqueId()), eventClass);
    }

    private boolean validateUUID(UUID uuid) {
        S managedPlayer = managedPlayerMap.get(uuid);
        return managedPlayer != null && managedPlayer.isInGame();
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
                    managedPlayer = wrapper.wrapPlayer(getArena(), player);
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

        manager.removeArena(getArena());
    }

    public abstract boolean canAcceptNew(Player player);

    public abstract boolean canAcceptExisting(S player);

    protected abstract T getArena();
}