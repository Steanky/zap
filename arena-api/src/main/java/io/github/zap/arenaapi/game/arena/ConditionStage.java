package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConditionStage {
    private final Consumer<Player> applyConsumer;
    private final Consumer<Player> removeConsumer;

    private boolean active = false;

    public void apply(Player player) {
        if(!active) {
            applyConsumer.accept(player);
            active = true;
        }
        else {
            ArenaApi.warning("Tried to apply a PlayerCondition twice!");
        }
    }

    public void remove(Player player) {
        if(active) {
            removeConsumer.accept(player);
            active = false;
        }
        else {
            ArenaApi.warning("Tried to remove a PlayerCondition that was not applied yet!");
        }
    }
}
