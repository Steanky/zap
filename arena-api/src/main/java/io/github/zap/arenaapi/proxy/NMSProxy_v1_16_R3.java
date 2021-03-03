package io.github.zap.arenaapi.proxy;

import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

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
}
