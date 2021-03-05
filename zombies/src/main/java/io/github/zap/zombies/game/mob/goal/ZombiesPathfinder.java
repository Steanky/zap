package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * General pathfinding class for Zombies. Supports lazy loading of entity metadata from MythicMobs; subclass pathfinding
 * functions will not be called until all required metadata has been loaded.
 */
public abstract class ZombiesPathfinder extends PathfinderGoal {
    @Getter
    private final AbstractEntity entity;

    @Getter
    private final EntityInsentient handle;

    @Getter
    private final String[] metadataKeys;

    @Getter
    private final ZombiesNMSProxy proxy;

    private final Map<String, Object> metadata = new HashMap<>();
    private boolean metadataLoaded;

    public ZombiesPathfinder(AbstractEntity entity, String... metadataKeys) {
        super();
        this.entity = entity;

        Entity nmsEntity = ((CraftEntity)entity.getBukkitEntity()).getHandle();
        if(nmsEntity instanceof EntityInsentient) {
            this.handle = (EntityInsentient)nmsEntity;
            this.metadataKeys = metadataKeys;
            this.metadataLoaded = metadataKeys.length == 0;
            proxy = Zombies.getInstance().getNmsProxy();
        }
        else {
            throw new UnsupportedOperationException("Tried to add PathfinderGoal to an Entity that does not subclass" +
                    " EntityInsentient!");
        }
    }

    /**
     * Gets the metadata value for the given string. Will throw ClassCastException if the metadata type does not match.
     * @param key The name of the metadata to get
     * @param <T> The type of the metadata
     * @return The metadata, after casting to T
     */
    public <T> T getMetadata(String key) {
        //noinspection unchecked
        return (T)metadata.get(key);
    }

    /**
     * Gets the metadata value for the given string. Will throw ClassCastException if the metadata type does not match.
     * Accepts a generic Class, to whose type the metadata will be cast. Null values for metadata are not permitted.
     * @param key The name of the metadata to get
     * @param dummy The Class which supplies the generic type parameter
     * @param <T> The type of the metadata
     * @return The metadata, after casting to T
     */
    public <T> T getMetadata(String key, Class<T> dummy) {
        //noinspection unchecked
        return (T)metadata.get(key);
    }

    @Override
    public final boolean shouldActivate() {
        if(!metadataLoaded) {
            for(String key : metadataKeys) {
                Optional<Object> optional = entity.getMetadata(key);
                if(optional.isPresent()) {
                    metadata.put(key, optional.get());
                }
                else {
                    return false;
                }
            }

            metadataLoaded = true;
        }

        return canStart();
    }

    @Override
    public final boolean shouldStayActive() {
        return stayActive();
    }

    @Override
    public final void start() {
        onStart();
    }

    @Override
    public final void onTaskReset() {
        onEnd();
    }

    @Override
    public final void tick() {
        doTick();
    }

    public abstract boolean canStart();

    public abstract boolean stayActive();

    public abstract void onStart();

    public abstract void onEnd();

    public abstract void doTick();
}
