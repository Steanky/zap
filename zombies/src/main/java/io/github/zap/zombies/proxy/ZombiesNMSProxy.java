package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.arenaapi.proxy.NMSProxy;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.util.ItemStackDescription;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable WrappedSignedProperty getSkin(@NotNull Player player);

    ZombiesPlayer findClosest(EntityInsentient entity, ZombiesArena arena, int deviation, Predicate<ZombiesPlayer> filter);

    void navigateToLocation(EntityInsentient entity, double x, double y, double z, double speed);

    double getDistanceToSquared(Entity entity, double x, double y, double z);

    void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2);

    void setDoubleFor(EntityLiving entity, AttributeBase attribute, double value);

    ItemStack getItemStackFromDescription(ItemStackDescription info) throws CommandSyntaxException;

    PathEntity calculatePathTo(EntityInsentient entity, Entity target, int deviation);

    boolean moveAlongPath(EntityInsentient entity, PathEntity path, double speed);

    boolean hasAttribute(EntityInsentient entity, AttributeBase attribute);
}
