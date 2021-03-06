package io.github.zap.arenaapi.game.arena;

import io.github.zap.arenaapi.ArenaApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConditionStage {
    private final Consumer<Player> applyConsumer;
    private final Consumer<Player> removeConsumer;

    @Getter
    private final boolean additive;

    @Getter
    private boolean active = false;

    public void apply(Player player) {
        if(!active) {
            applyConsumer.accept(player);
            active = true;
        }
    }

    public void remove(Player player) {
        if(active) {
            removeConsumer.accept(player);
            active = false;
        }
    }
}
