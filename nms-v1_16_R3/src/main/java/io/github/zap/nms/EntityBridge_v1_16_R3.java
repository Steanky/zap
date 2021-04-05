package io.github.zap.nms;

import io.github.zap.nms.entity.EntityBridge;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

class EntityBridge_v1_16_R3 implements EntityBridge {
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
        Optional<EntityTypes<?>> optional = EntityTypes.getByName(type.getKey().getKey());
        return optional.map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }
}
