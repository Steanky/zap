package io.github.zap.zombies.proxy;

import net.minecraft.server.v1_16_R3.*;


/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy {

    double getDistanceToSquared(Entity entity, double x, double y, double z);

    void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2);

    void setDoubleFor(EntityLiving entity, AttributeBase attribute, double value);

    boolean moveAlongPath(EntityInsentient entity, PathEntity path, double speed);

    boolean hasAttribute(EntityInsentient entity, AttributeBase attribute);
}
