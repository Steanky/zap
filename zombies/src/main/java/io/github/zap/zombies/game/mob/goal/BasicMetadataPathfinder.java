package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.arenaapi.pathfind.PathResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BasicMetadataPathfinder extends ZombiesPathfinder {
    protected ZombiesPlayer zombiesPlayer;
    private ZombiesArena arena;
    private final double speed;
    private final int targetDeviation;

    public BasicMetadataPathfinder(AbstractEntity entity, AttributeValue[] values, double speed, int targetDeviation) {
        super(entity, values, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
        this.speed = speed;
        this.targetDeviation = targetDeviation;
    }

    protected @Nullable PathResult retarget() {
        if(arena == null) {
            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
        }

        if(arena != null) {
            Set<ZombiesPlayer> validTargets = arena.getPlayerMap().values().stream().filter(ZombiesPlayer::isAlive)
                    .collect(Collectors.toSet());

            if(!validTargets.isEmpty()) {
                getHandler().queueOperation(PathOperation.forEntityWalking(getEntity().getBukkitEntity(),
                        validTargets, arena.getMapBounds(), targetDeviation), self.getWorld().getWorld());

                PathResult result = getHandler().tryTakeResult();

                if(result != null) {
                    ZombiesPlayer zombiesPlayer = (ZombiesPlayer)result.destination();
                    Player player = zombiesPlayer.getPlayer();

                    if(player != null) {
                        self.setGoalTarget(((CraftPlayer)player).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, false);
                        this.zombiesPlayer = zombiesPlayer;
                        return result;
                    }

                    this.zombiesPlayer = null;
                }
            }
        }

        return null;
    }

    protected void setPath(@NotNull PathResult result) {
        getNavigator().navigateAlongPath(result.toPathEntity(), speed);
    }
}
