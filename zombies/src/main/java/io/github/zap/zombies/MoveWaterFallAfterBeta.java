package io.github.zap.zombies;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.zombies.game.ZombiesArena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Function;

public class MoveWaterFallAfterBeta implements Listener {
    @Getter
    @Setter
    private Location lobbyLocation;


    public MoveWaterFallAfterBeta() {
        ArenaApi.getInstance().getArenaMangers().forEach((l,r) -> r.getArenaCreated().registerHandler(this::onArenaCreated));
    }


    private void onArenaCreated(Arena<?> arena) {
        if(arena instanceof ManagingArena) {
            ((ManagingArena<?, ? extends ManagedPlayer<?,?>>)arena).getOnDisposing().registerHandler(this::onArenaDisposing);
        }
    }

    private void onArenaDisposing(ManagingArena.ArenaEventArgs<?, ? extends ManagedPlayer<?, ?>> arenaEventArgs) {
        arenaEventArgs.getArena().getPlayerMap().forEach((l,r) -> {
            if(r.isInGame()) {
                r.getPlayer().teleport(lobbyLocation);
            }
        });
    }
}
