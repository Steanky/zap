package io.github.zap.zombies.event.player;

import io.github.zap.arenaapi.event.CustomEvent;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.WindowData;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Value
public class PlayerRepairWindowEvent extends CustomEvent {
    ZombiesPlayer player;
    WindowData window;
    int increment;
}
