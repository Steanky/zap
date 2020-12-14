package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.event.Event;
import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class ManagingArena<T extends ManagingArena<T, V>, V extends ManagedPlayer<V, T>> extends Arena<T> {
    @Value
    public class PlayerJoinEventArgs {
        List<V> joiningPlayers;
    }

    @Value
    public class PlayerLeaveEventArgs {
        List<V> leavingPlayers;
    }

    protected final Event<PlayerJoinEventArgs> playerJoinEvent = new Event<>();
    protected final Event<PlayerLeaveEventArgs> playerLeaveEvent = new Event<>();

    protected final ManagedPlayerBuilder<V, T> playerWrapper;
    protected final Map<UUID, V> managedPlayers = new HashMap<>();

    public ManagingArena(ArenaManager<T> manager, World world, ManagedPlayerBuilder<V, T> wrapper) {
        super(manager, world);
        playerWrapper = wrapper;
    }

    @Override
    public boolean handleJoin(JoinInformation joinAttempt) {
        if(joinAllowed(joinAttempt)) {
            Set<Player> players = joinAttempt.getJoinable().getPlayers();
            List<V> joiningPlayers = new ArrayList<>();

            for(Player player : players) {
                UUID uuid = player.getUniqueId();
                V managedPlayer = managedPlayers.get(uuid);

                if(managedPlayer != null) {
                    managedPlayer.rejoin();
                }
                else {
                    managedPlayer = playerWrapper.wrapPlayer(getArena(), player);
                    managedPlayers.put(uuid, managedPlayer);
                }

                joiningPlayers.add(managedPlayer);
            }

            if(joiningPlayers.size() > 0) {
                playerJoinEvent.callEvent(new PlayerJoinEventArgs(joiningPlayers));
            }
            return true;
        }

        return false;
    }

    @Override
    public void handleLeave(LeaveInformation leaveInformation) {
        List<V> leavingPlayers = new ArrayList<>();

        for(Player player : leaveInformation.getJoinable().getPlayers()) {
            V managedPlayer = managedPlayers.get(player.getUniqueId());

            if(managedPlayer != null) {
                managedPlayer.quit();
                leavingPlayers.add(managedPlayer);

                //TODO: teleport to target lobby
            }
        }

        if(leavingPlayers.size() > 0) {
            playerLeaveEvent.callEvent(new PlayerLeaveEventArgs(leavingPlayers));
        }
    }

    @Override
    public void close() {
        //close players
        for(V player : managedPlayers.values()) {
            player.close();
        }

        manager.removeArena(getArena());
    }

    public abstract boolean joinAllowed(JoinInformation attempt);

    public abstract T getArena();
}
