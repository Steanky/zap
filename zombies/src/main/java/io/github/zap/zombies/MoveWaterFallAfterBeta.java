package io.github.zap.zombies;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ManagedPlayer;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
                //TODO: REMOVE THIS AFTER BETA!!! this is a really bad way to do this
                r.getPlayer().teleport(lobbyLocation);
                r.getPlayer().setHealth(20);
                r.getPlayer().setLevel(0);
                r.getPlayer().setAllowFlight(false);
                r.getPlayer().setFoodLevel(20);
                r.getPlayer().setInvulnerable(true);
                r.getPlayer().setInvisible(false);
                r.getPlayer().setArrowsInBody(0); //i love that this is a thing lol
                r.getPlayer().getInventory().setStorageContents(new ItemStack[0]); //if we add items in lobby, replace this
                r.getPlayer().setWalkSpeed(2);
            }
        });
    }
}
