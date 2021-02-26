package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.ZombiesPlayerState;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.proxy.NavigationProxy;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Creature;

@MythicAIGoal(
        author = "Steank",
        name = "unboundedPlayerTarget",
        description = "Special goal that only cares about players in the current game and has an infinite range."
)
public class PathfinderGoalTargetPlayerUnbounded extends Pathfinder implements PathfindingGoal {
    private final Creature creature;
    private final NavigationProxy navigationProxy;

    private int tickCount;
    private ZombiesArena arena = null;
    private ZombiesPlayer target;

    private boolean loadedMetadata = false;
    private int mobRetargetTicks = 0;
    private boolean shouldRetarget = false;

    public PathfinderGoalTargetPlayerUnbounded(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        creature = (Creature) entity.getBukkitEntity();
        navigationProxy = Zombies.getInstance().getNmsProxy().getNavigationFor(creature);
    }

    private void loadMetadata() {
        if(entity.getMetadata(Zombies.ARENA_METADATA_NAME).isPresent()) {
            arena = (ZombiesArena) entity.getMetadata(Zombies.ARENA_METADATA_NAME).get();
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
                ai().setTarget(creature, null);
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
     * Sets the closest valid player as this entity's target.
     */
    private void retarget() {
        target = navigationProxy.findClosest(arena);
        ai().setTarget(creature, target.getPlayer());
    }
}
