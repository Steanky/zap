package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathHandler;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashSet;

public abstract class StandardMetadataPathfinder extends ZombiesPathfinder {
    public StandardMetadataPathfinder(AbstractEntity entity, AttributeValue[] values) {
        super(entity, values, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
    }

    @Override
    public boolean acquireTarget() {
        ZombiesArena arenaMetadata = getMetadata(Zombies.ARENA_METADATA_NAME);
        if(arenaMetadata != null) {
            getHandler().queueOperation(PathOperation.forEntityWalking(getEntity().getBukkitEntity(),
                    new HashSet<>(arenaMetadata.getPlayerMap().values()), 5),
                    getHandle().getWorld().getWorld());

            PathHandler.Entry result = getHandler().takeResult();
            if(result != null) {
                ZombiesPlayer target = (ZombiesPlayer)result.getResult().destination();
                getHandle().setGoalTarget((EntityLiving) target.getPlayer(), EntityTargetEvent.TargetReason.CUSTOM, false);
                return true;
            }
        }
        return false;
    }
}
