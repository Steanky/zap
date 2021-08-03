package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathDestination;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.arenaapi.pathfind.PathResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BasicMetadataPathfinder extends ZombiesPathfinder {
    protected ZombiesPlayer zombiesPlayer;
    private ZombiesArena arena;
    private final double speed;
    private final double targetDeviation;
    protected PathResult result;

    public BasicMetadataPathfinder(Mob mob, AttributeValue[] values, int retargetTicks, double speed,
                                   double targetDeviation) {
        super(mob, values, retargetTicks, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
        this.speed = speed;
        this.targetDeviation = targetDeviation;
    }

    protected void retarget() {
        if(arena == null) {
            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
        }

        if(arena != null) {
            //player.getPlayer() should NEVER throw an NPE, since we just checked isAlive in the stream
            Set<PathDestination> validTargets = arena.getPlayerMap().values().stream().filter(ZombiesPlayer::isAlive)
                    .map(player -> PathDestination.fromEntity(Objects.requireNonNull(player.getPlayer()), player, true))
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            if(!validTargets.isEmpty()) {
                PathOperation operation = PathOperation.forEntityWalking(self, validTargets, arena.getMapBounds(),
                        targetDeviation);

                if(operation != null) {
                    getHandler().queueOperation(operation, self.getWorld());

                    PathResult result = getHandler().tryTakeResult();

                    if(result != null) {
                        if(result.destination().target() instanceof ZombiesPlayer zombiesPlayer) {
                            Player player = zombiesPlayer.getPlayer();

                            if(player != null) {
                                self.setTarget(player);
                                this.zombiesPlayer = zombiesPlayer;
                                this.result = result;
                            }
                            else {
                                this.zombiesPlayer = null;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void setPath(@NotNull PathResult result) {
        getNavigator().navigateAlongPath(result.toPathEntity(), speed);
    }
}
