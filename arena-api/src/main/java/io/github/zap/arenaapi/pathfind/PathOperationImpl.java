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

    private final NavigableSet<PathNode> sortedNodes = new TreeSet<>();
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
                        onSuccess();
                        return true;
                    }
                }

                if(!sortedNodes.isEmpty()) {
                    currentNode = sortedNodes.pollFirst();
                }
                else {
                    onFailure();
                    return true;
                }
            }
            else {
                currentNode = agent.nodeAt();
            }

            visited.add(currentNode);
            PathNode[] possibleNodes = provider.generateNodes(context, this, currentNode);

            for(PathNode node : possibleNodes) {
                PathDestination closestDestination = closestDestinationFor(node);

                if(closestDestination != null) {
                    Cost possibleCost = calculator.computeCost(context, currentNode, node, closestDestination);

                    if(sortedNodes.contains(node)) {
                        if(possibleCost.nodeCost > currentNode.cost.nodeCost) {
                            continue;
                        }
                    }

                    sortedNodes.add(node);
                }
            }

            if(!sortedNodes.isEmpty()) {
                currentNode.next = sortedNodes.first();
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
        return agent.getWorld();
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

    private void onSuccess() {
        state = State.SUCCEEDED;

        //do things (compile path, etc)
    }

    private void onFailure() {
        state = State.FAILED;
    }

    private @Nullable PathDestination closestDestinationFor(@NotNull PathNode node) {
        int bestDistance = Integer.MAX_VALUE;
        PathDestination bestDestination = null;

        for(PathDestination destination : destinations) {
            int sample = node.distanceSquaredTo(destination.targetNode());

            if(sample < bestDistance) {
                bestDistance = sample;
                bestDestination = destination;
            }
        }

        return bestDestination;
    }
}
