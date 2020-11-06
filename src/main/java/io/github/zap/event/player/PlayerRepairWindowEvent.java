package io.github.zap.event.player;

import io.github.zap.event.CustomEvent;
import io.github.zap.game.arena.ZombiesPlayer;
import io.github.zap.game.data.WindowData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PlayerRepairWindowEvent extends CustomEvent {
    ZombiesPlayer player;
    WindowData window;
}
