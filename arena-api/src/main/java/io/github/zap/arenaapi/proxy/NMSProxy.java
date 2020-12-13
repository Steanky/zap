package io.github.zap.arenaapi.proxy;

import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * Access NMS classes through this proxy.
 */
public interface NMSProxy {

    /**
     * Gets the next entity id
     * @return The new id
     */
    int nextEntityId();

    /**
     * Creates a random UUID according to the NMS entity UUID random generator
     * @return The random UUID
     */
    UUID randomUUID();

    /**
     * Gets the true typeid of a living entity type
     * @param entityType The type of the living entity
     * @return The living entity typeid
     */
    int getEntityLivingTypeId(EntityType entityType);
}
