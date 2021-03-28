package io.github.zap.arenaapi.pathfind;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private static final int INITIAL_CAPACITY = 128;

    private final Mob mob;
    private final Set<PathDestination> destinations;
    private State state;
    private final float targetDistance;
    private final CostCalculator calculator;
    private final TerminationCondition condition;
    private final NodeProvider provider;

    private final PriorityQueue<PathNode> consideredNodes = new PriorityQueue<>(INITIAL_CAPACITY);
    private final Set<PathNode> closedSet = new HashSet<>();

    private boolean complete = false;
    private PathNode currentNode;

    PathOperationImpl(@NotNull Mob mob, @NotNull Set<PathDestination> destinations, float targetDistance,
                      @NotNull CostCalculator calculator, @NotNull TerminationCondition condition,
                      @NotNull NodeProvider provider) {
        this.mob = mob;
        this.destinations = destinations;
        this.state = State.INCOMPLETE;
        this.targetDistance = targetDistance;
        this.calculator = calculator;
        this.condition = condition;
        this.provider = provider;
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(!complete) {
            if(currentNode != null) {
                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        state = State.SUCCEEDED;
                        onSuccess();
                        return false;
                    }
                }
            }
            else {
                Location location = mob.getLocation();
                currentNode = new PathNode(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            }

            List<PathNode> possibilities = provider.nodesFrom(context, currentNode);
        }

        return true;
    }

    @Override
    public @NotNull PathOperation.State getState() {
        return state;
    }

    @Override
    public @NotNull PathResult getResult() {
        return null;
    }

    @Override
    public int desiredIterations() {
        return 0;
    }

    @Override
    public boolean shouldRemove() {
        return false;
    }

    @Override
    public @NotNull World getWorld() {
        return mob.getWorld();
    }

    @Override
    public @NotNull Set<PathDestination> getDestinations() {
        return destinations;
    }

    private void onSuccess() {

    }
}
