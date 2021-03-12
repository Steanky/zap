package io.github.zap.arenaapi.proxy;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.EntityType;

import java.util.Optional;
import java.util.UUID;

public class NMSProxy_v1_16_R3 implements NMSProxy {
    @Override
    public int nextEntityId() {
        return Entity.nextEntityId();
    }

    @Override
    public UUID randomUUID() {
        return MathHelper.a(Entity.SHARED_RANDOM);
    }

    @Override
    public int getEntityTypeId(EntityType entityType) {
        Optional<EntityTypes<?>> opt = EntityTypes.getByName(entityType.getKey().getKey());
        return opt.map(IRegistry.ENTITY_TYPE::a).orElse(-1);
    }

    @Override
    public String getDefaultWorldName() {
        return MinecraftServer.c.getLevelName();
    }
}
