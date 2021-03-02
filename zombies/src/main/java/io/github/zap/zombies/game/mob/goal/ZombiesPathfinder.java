package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * General pathfinding class. Supports loading of MythicMobs entity metadata and exposes the ZombiesNMSProxy.
 */
public abstract class ZombiesPathfinder extends Pathfinder implements PathfindingGoal {
    private final Map<String, Object> metadata = new HashMap<>();
    private final String[] keys;
    private boolean metadataLoaded;

    @Getter
    private final EntityInsentient nmsEntity;

    @Getter
    private final ZombiesNMSProxy proxy;

    public ZombiesPathfinder(AbstractEntity entity, String line, MythicLineConfig mlc, String... metadataKeys) {
        super(entity, line, mlc);
        keys = metadataKeys;
        nmsEntity = (EntityInsentient) ((CraftEntity)entity.getBukkitEntity()).getHandle();
        proxy = Zombies.getInstance().getNmsProxy();
    }

    private boolean loadMetadata() {
        if(!metadataLoaded) {
            for(String key : keys) {
                Optional<Object> object = entity.getMetadata(key);
                if(object.isPresent()) {
                    metadata.put(key, object.get());
                }
                else {
                    return false;
                }
            }

            metadataLoaded = true;
        }

        return true;
    }

    public <T> T getMetadata(String name) {
        //noinspection unchecked
        return (T)metadata.get(name);
    }

    @Override
    public final boolean shouldStart() {
        return loadMetadata() && canStart();
    }

    @Override
    public final boolean shouldEnd() {
        return canEnd();
    }

    public abstract boolean canStart();

    public abstract boolean canEnd();
}
