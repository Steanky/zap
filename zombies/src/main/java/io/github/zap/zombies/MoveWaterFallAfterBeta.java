package io.github.zap.zombies;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.*;
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
                r.getPlayer().teleport(lobbyLocation);
                ArenaPlayer player = ArenaApi.getInstance().getArenaPlayer(r.getPlayer().getUniqueId());
                player.removeConditionContext(arenaEventArgs.getArena().toString());
                ArenaApi.getInstance().applyDefaultStage(player);
            }
        });
    }
}
