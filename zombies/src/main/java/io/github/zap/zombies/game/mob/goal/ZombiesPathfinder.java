package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.pathfind.PathHandler;
import io.github.zap.arenaapi.pathfind.PathfinderEngine;
import io.github.zap.arenaapi.shadow.io.github.zap.nms.common.pathfind.MobNavigator;
import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.MetadataValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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

    @Getter
    private final MobNavigator navigator;

    @Getter
    private final PathHandler handler;

    private final Map<String, Object> metadata = new HashMap<>();
    private boolean metadataLoaded;

    public ZombiesPathfinder(AbstractEntity entity, AttributeValue[] values, String... metadataKeys) {
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

        try {
            navigator = ArenaApi.getInstance().getNmsBridge().entityBridge().overrideNavigatorFor((Mob)entity.getBukkitEntity());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UnsupportedOperationException("Failed to reflect entity navigator!");
        }

        handler = new PathHandler(PathfinderEngine.async());

        if(handle instanceof EntitySkeletonAbstract) {
            try {
                Field bowShootGoal = EntitySkeletonAbstract.class.getDeclaredField("b");
                Field meleeAttackGoal = EntitySkeletonAbstract.class.getDeclaredField("c");

                bowShootGoal.setAccessible(true);
                meleeAttackGoal.setAccessible(true);

                bowShootGoal.set(handle, new DummyPathfinderGoalBowShoot<>((EntitySkeletonAbstract)handle));
                meleeAttackGoal.set(handle, new DummyPathfinderGoalMeleeAttack((EntityCreature)handle));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Zombies.warning("Failed to set AI field on EntitySkeletonAbstract due to a reflection-related exception.");
            }
        }

        for(AttributeValue value : values) {
            getProxy().setDoubleFor(handle, value.getAttribute(), value.getValue());
        }
    }

    /**
     * Gets the metadata value for the given string. Will throw ClassCastException if the metadata type does not match.
     * Will return null if the metadata itself is null, or if there is no metadata associated with the given value.
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
     * Accepts a generic Class, to whose type the metadata will be cast.
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
                MetadataValue value = MetadataHelper.getMetadataFor(entity.getBukkitEntity(), Zombies.getInstance(), key);

                if(value != null) {
                    this.metadata.put(key, value.value());
                }
                else {
                    return false;
                }
            }

            metadataLoaded = true;
        }

        if(handle.getGoalTarget() == null) {
            return acquireTarget() && canStart();
        }
        else{
            return canStart();
        }
    }

    @Override
    public final boolean shouldStayActive() {
        return stayActive() && handle.getGoalTarget() != null;
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

    public abstract boolean acquireTarget();

    public abstract boolean canStart();

    public abstract boolean stayActive();

    public abstract void onStart();

    public abstract void onEnd();

    public abstract void doTick();
}
