package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.proxy.NMSProxy;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy extends NMSProxy {
    NavigationProxy getNavigationFor(Creature creature);
}
