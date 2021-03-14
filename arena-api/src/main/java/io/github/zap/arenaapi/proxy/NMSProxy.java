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
     * Gets the true typeid of an entity type
     * @param entityType The type of the entity
     * @return The entity typeid
     */
    int getEntityTypeId(EntityType entityType);

    /**
     * Gets the default world name of the server
     * @return the default world name of the server
     */
    String getDefaultWorldName();

}
