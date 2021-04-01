package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final Set<? extends PathDestination> destinations;
    private State state;
    private final ScoreCalculator calculator;
    private final TerminationCondition condition;
    private final NodeProvider provider;
    private final DestinationSelector selector;

    private final NodeQueue openSet = new NodeQueue();
    private final Set<PathNode> visited = new HashSet<>();
    private PathDestination destination;
    private PathNode currentNode;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                      @NotNull ScoreCalculator calculator, @NotNull TerminationCondition condition,
                      @NotNull NodeProvider provider, @NotNull DestinationSelector selector) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.INCOMPLETE;
        this.calculator = calculator;
        this.condition = condition;
        this.provider = provider;
        this.selector = selector;
    }

    @SneakyThrows
    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.INCOMPLETE) {
            if(currentNode != null) {
                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        complete(true, destination);
                        return true;
                    }
                }

                if(openSet.size() != 0) {
                    currentNode = openSet.poll();
                }
                else {
                    complete(false, destination);
                    return true;
                }
            }
            else {
                currentNode = agent.nodeAt();
                currentNode.score = new Score(0, calculator.computeH(context, currentNode, selector.selectDestinationFor(this, currentNode)));
            }

            visited.add(currentNode);

            Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                Block block = context.snapshotProvider().getWorld().getBlockAt(currentNode.x, currentNode.y - 1, currentNode.z);
                block.setType(Material.GREEN_WOOL);
            });

            Thread.sleep(50);

            List<PathNode> possibleNodes = provider.generateNodes(context, this, currentNode);

            for(PathNode sample : possibleNodes) {
                if(sample == null) {
                    break;
                }

                if(visited.contains(sample)) {
                    continue;
                }

                destination = selector.selectDestinationFor(this, sample);
                double g = calculator.computeG(context, currentNode, sample, destination);

                if(g < sample.score.g) {
                    sample.parent = currentNode;

                    openSet.update(sample, node -> {
                        node.score = new Score(g, calculator.computeH(context, sample, destination));
                    });
                }

                Bukkit.getScheduler().runTask(ArenaApi.getInstance(), () -> {
                    Block block = context.snapshotProvider().getWorld().getBlockAt(sample.x, sample.y - 1, sample.z);
                    block.setType(Material.RED_WOOL);
                });

                Thread.sleep(50);
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

        if(result == null) {
            throw new IllegalStateException("Result has not been compiled for " + this);
        }

        return result;
    }

    @Override
    public int desiredIterations() {
        return 200;
    }

    @Override
    public boolean shouldRemove() {
        return true;
    }


    @Override
    public @NotNull Set<? extends PathDestination> getDestinations() {
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

    private void complete(boolean success, PathDestination destination) {
        this.state = success ? State.SUCCEEDED : State.FAILED;
        result = new PathResultImpl(currentNode, destination);
    }
}