package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.proxy.NMSProxy;
import org.bukkit.entity.Creature;

/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy extends NMSProxy {
    NavigationProxy getNavigationFor(Creature creature);
}
