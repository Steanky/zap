package io.github.zap.zombies.game.ai;

import io.github.zap.arenaapi.util.ListUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.ZombiesPlayerState;
import io.github.zap.zombies.game.data.MapData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.*;

@MythicAIGoal(
        name = "unboundedPlayerTarget",
        description = "Special goal that only cares about players in the current game and has an infinite range."
)
public class PathfinderGoalTargetPlayerUnbounded extends Pathfinder implements PathfindingGoal {
    private static final String ARENA_METADATA_NAME = "zombies_arena";

    private final EntityCreature nmsEntity;
    private int tickCount;
    private ZombiesArena arena = null;
    private ZombiesPlayer target;

    private boolean loadedMetadata = false;
    private int mobRetargetTicks = 0;
    private boolean shouldRetarget = false;

    public PathfinderGoalTargetPlayerUnbounded(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        nmsEntity = (EntityCreature)((CraftEntity)entity.getBukkitEntity()).getHandle();
    }

    private void loadMetadata() {
        if(entity.hasMetadata(ARENA_METADATA_NAME) && entity.getMetadata(ARENA_METADATA_NAME).isPresent()) {
            arena = (ZombiesArena) entity.getMetadata(ARENA_METADATA_NAME).get();
            loadedMetadata = true;
            MapData map = arena.getMap();
            mobRetargetTicks = map.getMobRetargetTicks();
            shouldRetarget = map.getMobRetargetTicks() != -1;
        }
    }

    @Override
    public boolean shouldStart() {
        if(!loadedMetadata) {
            loadMetadata();
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        retarget();
    }

    /**
     * Stop targeting if the player isn't in survival or adventure mode. If we haven't already targeted a player,
     * attempt to retarget every second. The latter should not happen during an actual game.
     */
    @Override
    public void tick() {
        if(target != null) {
            if(target.getState() != ZombiesPlayerState.ALIVE) { //our old target died
                ai().setTarget(nmsEntity.getBukkitLivingEntity(), null);
                retarget();
            }
        }
        else if(shouldRetarget) { //depending on how we are configured, try to retarget periodically
            tickCount++;

            if(tickCount >= mobRetargetTicks) {
                retarget();
                tickCount = 0;
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        return arena.getState() != ZombiesArenaState.STARTED;
    }

    @Override
    public void end() { }

    /**
     * Sets the closest valid player as this entity's target. Cannot target players who are not participating in a game,
     * or who are not in survival or adventure mode.
     */
    private void retarget() {
        NavigationAbstract navigator = nmsEntity.getNavigation();
        Map<BlockPosition, List<ZombiesPlayer>> positionMappings = new HashMap<>();

        for(ZombiesPlayer player : arena.getZombiesPlayers()) {
            Player bukkitPlayer = player.getPlayer();
            Location location = bukkitPlayer.getLocation();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(),
                    location.getBlockZ());

            positionMappings.putIfAbsent(blockPosition, new ArrayList<>());
            positionMappings.get(blockPosition).add(player);
        }

        PathEntity path = navigator.a(positionMappings.keySet(), 1024);
        if(path != null) {
            PathPoint finalPoint = path.getFinalPoint();

            if(finalPoint != null) {
                BlockPosition targetPosition = finalPoint.a();
                List<ZombiesPlayer> players = positionMappings.get(targetPosition);

                if(players != null) {
                    target = ListUtils.randomElement(players);
                    ai().setTarget(nmsEntity.getBukkitLivingEntity(), target.getPlayer());
                }
                else {
                    Zombies.warning(String.format("Resulting List<ZombiesPlayer> for BlockPosition %s is null.",
                            targetPosition));
                }
            }
            else {
                Zombies.warning("PathEntity#getFinalPoint() returned null. Player may be out of bounds.");
            }
        }
        else {
            Zombies.warning("NavigationAbstract#a(Set<BlockPosition>,int) returned null.");
        }
    }
}