package io.github.zap.zombies.proxy;

import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;

import java.util.Optional;
import java.util.UUID;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {
    @Override
    public NavigationProxy getNavigationFor(Creature creature) {
        return new NavigationProxy_v1_16_R3(((CraftCreature)creature).getHandle().getNavigation());
    }
}
