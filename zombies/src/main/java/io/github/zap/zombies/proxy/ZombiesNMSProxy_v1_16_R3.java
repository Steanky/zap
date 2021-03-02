package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
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

import java.util.function.Predicate;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {
    @Override
    public WrappedSignedProperty getSkin(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        return WrappedSignedProperty.fromHandle(
                craftPlayer.getProfile().getProperties().get("textures").iterator().next()
        );
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
    public void navigateToLocation(EntityInsentient entity, Location location, double minStopDistance) {
        if(entity.isAlive()) {
            entity.getNavigation().a(location.getX(), location.getY(), location.getZ(), minStopDistance);
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
}
