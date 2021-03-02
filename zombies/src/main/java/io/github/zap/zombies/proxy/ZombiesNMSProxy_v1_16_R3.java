package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.properties.Property;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Collection;
import java.util.function.Predicate;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {
    @Override
    public WrappedSignedProperty getSkin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Collection<Property> texture = craftPlayer.getProfile().getProperties().get("textures");

        if (texture.size() > 0) {
            return WrappedSignedProperty.fromHandle(texture.iterator().next());
        } else {
            return null;
        }
    }

    @Override
    public ZombiesPlayer findClosest(EntityInsentient entity, ZombiesArena arena, Predicate<ZombiesPlayer> filter) {
        Pair<Float, ZombiesPlayer> bestCandidate = ImmutablePair.of(Float.MAX_VALUE, null);

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            if(filter.test(player)) {
                Player bukkitPlayer = player.getPlayer();
                Location location = bukkitPlayer.getLocation();

                PathEntity path = entity.getNavigation().a(new BlockPosition(location.getX(), location.getY(), location.getZ()), 0);

                if(path != null) {
                    PathPoint finalPoint = path.getFinalPoint();
                    if(finalPoint != null) {
                        if(finalPoint.e < bestCandidate.getLeft()) {
                            bestCandidate = ImmutablePair.of(finalPoint.e, player);
                        }
                    }
                }
            }
        }

        return bestCandidate.getRight();
    }

    @Override
    public void navigateToLocation(EntityInsentient entity, double x, double y, double z, double speed) {
        if(entity.isAlive()) {
            entity.getNavigation().a(x, y, z, speed);
        }
    }

    @Override
    public double getDistanceToSquared(Entity entity, double x, double y, double z) {
        return entity.h(x, y, z);
    }

    @Override
    public void lookAtEntity(ControllerLook look, Entity target, float f1, float f2) {
        look.a(target, f1, f2);
    }

    @Override
    public void setTarget(EntityInsentient entity, EntityLiving target, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        entity.setGoalTarget(target, reason, fireEvent);
    }

    @Override
    public void setAttributeFor(EntityLiving entity, AttributeBase attribute, double value) {
        AttributeModifiable modifiableAttribute = entity.getAttributeMap().a(attribute);

        if(modifiableAttribute != null) {
            modifiableAttribute.setValue(value);
        }
    }
}
