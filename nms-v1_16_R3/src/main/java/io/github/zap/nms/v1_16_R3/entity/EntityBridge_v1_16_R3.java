package io.github.zap.nms.v1_16_R3.entity;

import io.github.zap.nms.common.entity.EntityBridge;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityBridge_v1_16_R3 implements EntityBridge {
    public static final EntityBridge_v1_16_R3 INSTANCE = new EntityBridge_v1_16_R3();

    private EntityBridge_v1_16_R3() {}

    @Override
    public int nextEntityID() {
        return Entity.nextEntityId();
    }

    @Override
    public @NotNull UUID randomUUID() {
        return MathHelper.a(Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeID(@NotNull EntityType type) {
        return (EntityTypes.getByName(type.getKey().getKey())).map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }
}
