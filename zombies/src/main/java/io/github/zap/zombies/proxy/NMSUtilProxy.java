package io.github.zap.zombies.proxy;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public interface NMSUtilProxy {

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
