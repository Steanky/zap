package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import io.github.zap.arenaapi.proxy.NMSProxy;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.function.Predicate;

/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy extends NMSProxy {
    /**
     * Gets a wrapped signed property of a player's skin texture
     * @param player The player to get the skin from
     * @return A wrapped sign property of the texture
     */
    WrappedSignedProperty getSkin(Player player);

    ZombiesPlayer findClosest(EntityInsentient entity, ZombiesArena arena, Predicate<ZombiesPlayer> filter);

    void navigateToLocation(EntityInsentient entity, double x, double y, double z, double speed);

    double getDistanceToSquared(Entity entity, double x, double y, double z);

    void lookAtEntity(ControllerLook look, Entity target, float f1, float f2);

    void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2);

    void setTarget(EntityInsentient entity, EntityLiving target, EntityTargetEvent.TargetReason reason, boolean fireEvent);

    void setAttributeFor(EntityLiving entity, AttributeBase attribute, double value);
}