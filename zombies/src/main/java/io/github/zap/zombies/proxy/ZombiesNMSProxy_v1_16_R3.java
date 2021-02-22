package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {
    @Override
    public NavigationProxy getNavigationFor(Creature creature) {
        return new NavigationProxy_v1_16_R3(((CraftCreature)creature).getHandle().getNavigation());
    }
}