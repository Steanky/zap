package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.zap.zombies.game.data.util.ItemStackDescription;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy {

    double getDistanceToSquared(Entity entity, double x, double y, double z);

    void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2);

    void setDoubleFor(EntityLiving entity, AttributeBase attribute, double value);

    ItemStack getItemStackFromDescription(ItemStackDescription info) throws CommandSyntaxException;

    boolean moveAlongPath(EntityInsentient entity, PathEntity path, double speed);

    boolean hasAttribute(EntityInsentient entity, AttributeBase attribute);
}
