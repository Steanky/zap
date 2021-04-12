package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.util.ItemStackDescription;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class ZombiesNMSProxy_v1_16_R3 extends NMSProxy_v1_16_R3 implements ZombiesNMSProxy {

    @Override
    public @Nullable WrappedSignedProperty getSkin(@NotNull Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile gameProfile = craftPlayer.getProfile();
        if (gameProfile != null) {
            Collection<Property> texture = gameProfile.getProperties().get("textures");

            if (texture.size() > 0) {
                return WrappedSignedProperty.fromHandle(texture.iterator().next());
            }
        }

        return null;
    }

    @Override
    public ZombiesPlayer findClosest(EntityInsentient entity, ZombiesArena arena, int deviation, Predicate<ZombiesPlayer> filter) {
        Pair<Double, ZombiesPlayer> bestCandidate = Pair.of(Double.MAX_VALUE, null);

        for(ZombiesPlayer player : arena.getPlayerMap().values()) {
            if(filter.test(player)) {
                Player bukkitPlayer = player.getPlayer();

                if(bukkitPlayer != null) {
                    Location location = bukkitPlayer.getLocation();
                    double dist = entity.h(location.getX(), location.getY(), location.getZ());

                    if(dist < bestCandidate.getLeft()) {
                        bestCandidate = Pair.of(dist, player);
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
        AttributeMapBase attributeMap = entity.getAttributeMap();
        AttributeModifiable modifiableAttribute = attributeMap.a(attribute);

        if(modifiableAttribute != null) {
            modifiableAttribute.setValue(value);
        }
        else {
            attributeMap.registerAttribute(attribute);
            //noinspection ConstantConditions
            attributeMap.a(attribute).setValue(value);
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
    public PathEntity calculatePathTo(EntityInsentient entity, double x, double y, double z, int deviation) {
        return entity.getNavigation().a(x, y, z, deviation);
    }

    @Override
    public PathEntity calculatePathTo(EntityInsentient entity, Entity target, int deviation) {
        return entity.getNavigation().calculateDestination(target);
    }

    @Override
    public boolean moveAlongPath(EntityInsentient entity, PathEntity path, double speed) {
        return entity.getNavigation().a(path, speed);
    }

    @Override
    public boolean navigateToEntity(EntityInsentient entity, Entity target, double speed) {
        return entity.getNavigation().a(target, speed);
    }

    @Override
    public boolean hasAttribute(EntityInsentient entity, AttributeBase attribute) {
        return entity.getAttributeMap().b(attribute);
    }
}
