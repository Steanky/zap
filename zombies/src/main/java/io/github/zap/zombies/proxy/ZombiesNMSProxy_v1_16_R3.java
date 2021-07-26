package io.github.zap.zombies.proxy;

import net.minecraft.server.v1_16_R3.*;

public class ZombiesNMSProxy_v1_16_R3 implements ZombiesNMSProxy {

    @Override
    public double getDistanceToSquared(Entity entity, double x, double y, double z) {
        return entity.h(x, y, z);
    }

    @Override
    public void lookAtPosition(ControllerLook look, double x, double y, double z, float f1, float f2) {
        look.a(x, y, z, f1, f2);
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
    public boolean moveAlongPath(EntityInsentient entity, PathEntity path, double speed) {
        return entity.getNavigation().a(path, speed);
    }

    @Override
    public boolean hasAttribute(EntityInsentient entity, AttributeBase attribute) {
        return entity.getAttributeMap().b(attribute);
    }
}
