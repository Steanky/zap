package io.github.zap.zombies.proxy;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;

import java.util.function.Predicate;

public interface NavigationProxy {
    ZombiesPlayer findClosest(ZombiesArena arena, Predicate<ZombiesPlayer> filter);
}
