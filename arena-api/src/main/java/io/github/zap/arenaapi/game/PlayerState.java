package io.github.zap.arenaapi.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Keeps track of the player's state.
 */
@RequiredArgsConstructor
public class PlayerState {
    private final Consumer<Player> apply;
    private final Consumer<Player> undo;

    public void apply(Player player) {
        apply.accept(player);
    }

    public void undo(Player player) {
        undo.accept(player);
    }

    public static void transfer(PlayerState from, PlayerState to, Player player) {
        from.undo(player);
        to.apply(player);
    }
}
