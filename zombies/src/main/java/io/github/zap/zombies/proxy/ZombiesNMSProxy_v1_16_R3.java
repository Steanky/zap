package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.util.ItemStackDescription;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

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

    /**
     * Returns the nearest ZombiesPlayer in the given arena, using path length instead of vector distance for AI that
     * should prioritize rationally. Uses a predicate — ZombiesPlayers who fail the predicate will not be considered.
     * @param entity The entity to navigate for
     * @param arena The arena to search in
     * @param filter The predicate to use
     * @return The nearest ZombiesPlayer using path length, or null if none exist that are reachable and match the
     * predicate
     */
    @Override
    public ZombiesPlayer findClosest(EntityInsentient entity, ZombiesArena arena, Predicate<ZombiesPlayer> filter) {
        Pair<Float, ZombiesPlayer> bestCandidate = ImmutablePair.of(Float.MAX_VALUE, null);

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            if(filter.test(player)) {
                Player bukkitPlayer = player.getPlayer();
                PathEntity path = getPathToUnbounded(entity, ((CraftPlayer)bukkitPlayer).getHandle(), 0);

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
            NavigationAbstract navigationAbstract = entity.getNavigation();
            entity.getNavigation().a(navigationAbstract.a(x, y, z, 0), speed);
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
    public void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2) {
        look.a(x, y, z, f1, f2);
    }

    @Override
    public void setTarget(EntityInsentient entity, EntityLiving target, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        entity.setGoalTarget(target, reason, fireEvent);
    }

    @Override
    public void setDoubleFor(EntityLiving entity, AttributeBase attribute, double value) {
        AttributeModifiable modifiableAttribute = entity.getAttributeMap().a(attribute);

        if(modifiableAttribute != null) {
            modifiableAttribute.setValue(value);
        }
    }

    @Override
    public ItemStack getItemStackFromDescription(ItemStackDescription info) throws CommandSyntaxException {

        var itemStack = new ItemStack(info.getMaterial(), info.getCount());
        if(info.getNbt() != null && !info.getNbt().isEmpty()) {
            var nbt = MojangsonParser.parse(info.getNbt());
            var nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            nmsItemStack.setTag(nbt);
            return nmsItemStack.getBukkitStack();
        } else {
            return itemStack;
        }

    }

    @Override
    public PathEntity getPathToUnbounded(EntityInsentient entity, double x, double y, double z, int deviation) {
        NavigationAbstract navigationAbstract = entity.getNavigation();
        navigationAbstract.a(Float.MAX_VALUE);
        PathEntity path = navigationAbstract.a(x, y, z, deviation);
        navigationAbstract.g();
        return path;
    }

    @Override
    public PathEntity getPathToUnbounded(EntityInsentient entity, Entity target, int deviation) {
        NavigationAbstract navigationAbstract = entity.getNavigation();
        navigationAbstract.a(Float.MAX_VALUE);
        PathEntity path = navigationAbstract.calculateDestination(target);
        navigationAbstract.g();
        return path;
    }

    @Override
    public boolean navigateAlongPath(EntityInsentient entity, PathEntity path, double speed) {
        return entity.getNavigation().a(path, speed);
    }
}
