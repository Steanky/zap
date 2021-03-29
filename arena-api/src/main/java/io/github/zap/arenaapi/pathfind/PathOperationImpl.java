package io.github.zap.arenaapi.pathfind;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final Set<PathDestination> destinations;
    private State state;
    private final CostCalculator calculator;
    private final TerminationCondition condition;
    private final NodeProvider provider;

    private final NavigableSet<PathNode> openSet = new TreeSet<>();
    private final Set<PathNode> visited = new HashSet<>();

    private PathNode currentNode;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<PathDestination> destinations,
                      @NotNull CostCalculator calculator, @NotNull TerminationCondition condition,
                      @NotNull NodeProvider provider) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.INCOMPLETE;
        this.calculator = calculator;
        this.condition = condition;
        this.provider = provider;
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.INCOMPLETE) {
            if(currentNode != null) {
                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        success();
                        return true;
                    }
                }

                if(!openSet.isEmpty()) {
                    currentNode = openSet.pollFirst();
                }
                else {
                    failure();
                    return true;
                }
            }
            else {
                currentNode = agent.nodeAt();
            }

            visited.add(currentNode);
            PathNode[] possibleNodes = provider.generateNodes(context, this, currentNode);

            for(PathNode sample : possibleNodes) {
                if(visited.contains(sample)) {
                    continue;
                }

                PathDestination closestDestination = closestDestinationFor(sample);

                if(closestDestination != null) {
                    Cost sampleCost = calculator.computeCost(context, currentNode, sample, closestDestination);

                    if(openSet.contains(sample)) {
                        if(sampleCost.nodeCost > sample.cost.nodeCost) {
                            continue;
                        }
                    }

                    sample.cost = sampleCost;
                    openSet.add(sample);
                }
            }

            if(!openSet.isEmpty()) {
                currentNode.next = openSet.first();
            }
        }

        return false;
    }

    @Override
    public @NotNull PathOperation.State getState() {
        return state;
    }

    @Override
    public @NotNull PathResult getResult() {
        if(state == State.INCOMPLETE) {
            throw new IllegalStateException("Cannot get PathResult for a PathOperation that has not completed!");
        }

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
    public @NotNull Set<PathDestination> getDestinations() {
        return destinations;
    }

    @Override
    public @NotNull Set<PathNode> visitedNodes() {
        return visited;
    }

    @Override
    public @NotNull PathAgent getAgent() {
        return agent;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private void success() {
        state = State.SUCCEEDED;

        //do things (compile path, etc)
    }

    private void failure() {
        state = State.FAILED;
    }

    private @Nullable PathDestination closestDestinationFor(@NotNull PathNode node) {
        int bestDistance = Integer.MAX_VALUE;
        PathDestination bestDestination = null;

        for(PathDestination destination : destinations) {
            int sample = node.distanceSquaredTo(destination.node());

            if(sample < bestDistance) {
                bestDistance = sample;
                bestDestination = destination;
            }
        }

        return bestDestination;
    }
}
