package io.github.zap.zombies.proxy;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;

public interface NavigationProxy {
    ZombiesPlayer findClosest(ZombiesArena arena);
}
