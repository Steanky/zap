package io.github.zap.zombies.proxy;

import io.github.zap.zombies.Zombies;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.MathHelper;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NMS_v1_16_R3 implements NMSProxy {

    private AtomicInteger entityCount = null;

    @Override
    public AtomicInteger getEntityCount() {
        if (entityCount == null) {
            try {
                Field field = Entity.class.getDeclaredField("entityCount");
                field.setAccessible(true);

                entityCount = (AtomicInteger) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Zombies.getInstance().getLogger().warning("Error getting entityCount");
                return null;
            }
        }

        return entityCount;
    }

    @Override
    public UUID randomUUID() {
        return MathHelper.a(Entity.SHARED_RANDOM);
    }
}
