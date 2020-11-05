package io.github.zap.game.player;

import io.github.zap.game.arena.ZombiesArena;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ZombiesPlayer {
    @Getter
    private final Player player;

    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private PlayerState state;

    public void playerTick() {

    }

    public void playerRightClick() {

    }
}
