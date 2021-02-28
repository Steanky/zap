package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMob;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {
    @Override
    public NavigationProxy getNavigationFor(Mob mob) {
        return new NavigationProxy_v1_16_R3(((CraftMob)mob).getHandle().getNavigation());
    }

    @Override
    public WrappedSignedProperty getSkin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        return WrappedSignedProperty.fromHandle(
                craftPlayer.getProfile().getProperties().get("textures").iterator().next()
        );
    }
}