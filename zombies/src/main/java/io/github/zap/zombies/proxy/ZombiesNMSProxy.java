package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.proxy.NMSProxy;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy extends NMSProxy {
    NavigationProxy getNavigationFor(Mob mob);
}
