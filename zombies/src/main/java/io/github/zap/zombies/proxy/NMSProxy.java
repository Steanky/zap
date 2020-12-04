package io.github.zap.zombies.proxy;

import net.minecraft.server.v1_16_R3.EntityTypes;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface NMSProxy {

    AtomicInteger getEntityCount();

    UUID randomUUID();

    int getEntityLivingTypeId(EntityTypes<?> entityTypes);

}
