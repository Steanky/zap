package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final Set<PathDestination> destinations;
    private State state;
    private final ScoreCalculator calculator;
    private final SuccessCondition condition;
    private final NodeProvider nodeProvider;
    private final DestinationSelector selector;
    private final ChunkRange range;

    private final NodeQueue openSet = new NodeQueue();
    private final Set<PathNode> visited = new HashSet<>();
    private PathDestination destination;
    private PathNode firstNode;
    private PathNode currentNode;
    private PathNode bestFound;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<PathDestination> destinations,
                      @NotNull ScoreCalculator calculator, @NotNull SuccessCondition condition,
                      @NotNull NodeProvider nodeProvider, @NotNull DestinationSelector selector, @NotNull ChunkRange range) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.INCOMPLETE;
        this.calculator = calculator;
        this.condition = condition;
        this.nodeProvider = nodeProvider;
        this.selector = selector;
        this.range = range;
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.INCOMPLETE) {
            if(currentNode != null) {
                PathDestination best = null;
                double bestScore = Double.POSITIVE_INFINITY;
                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        ArenaApi.info("Terminating successfully.");
                        complete(true, destination);
                        return true;
                    }

                    double thisScore = destination.destinationScore(currentNode);
                    if(best == null || thisScore < bestScore) {
                        best = destination;
                        bestScore = thisScore;
                    }
                }

                if(best == null) {
                    throw new IllegalStateException("Unable to find a valid destination!");
                }

                if(openSet.size() != 0) {
                    currentNode = openSet.poll();
                }
                else {
                    complete(false, destination == null ? best : destination);
                    return true;
                }
            }
            else if(firstNode == null) {
                currentNode = agent.nodeAt();
                currentNode.score = new Score(0, calculator.computeH(context, currentNode, selector.selectDestinationFor(this, currentNode)));
                bestFound = new PathNode(currentNode.x, currentNode.y, currentNode.z);
                firstNode = currentNode;
            }
            else {
                throw new IllegalStateException("currentNode is null, but firstNode has already been set!");
            }

            visited.add(currentNode);

            List<PathNode> possibleNodes = nodeProvider.generateValidNodes(context, agent, currentNode);

            for(PathNode sample : possibleNodes) {
                if(sample == null || visited.contains(sample)) {
                    continue;
                }

                destination = selector.selectDestinationFor(this, sample);

                /*
                optimization: iterate failed paths. if the failed path has the same agent characteristics and the same
                destination, and its nodes are capable of reaching ours, we know that this operation will not be capable
                of reaching its destination, so we can remove it entirely. it is replaced by a destination that is known
                to be accessible — the endpoint of the failed node, which will be the node with the smallest h value. in
                order to make sure heuristic calculations are consistent, this special destination will report a score
                equal to the distance between the original destination (that is unreachable) and the node being sampled
                 */
                for(PathResult failed : context.failedPaths()) {
                    PathDestination failedDestination = failed.destination();
                    if(failedDestination.equals(destination) &&
                            failed.operation().agent().characteristics().equals(agent.characteristics()) &&
                            failed.visitedNodes().contains(sample) &&
                            failed.operation().nodeProvider().isValid(context, agent, sample, currentNode)) {
                        ArenaApi.info("Found an old failed path.");
                        destinations.remove(destination);
                        PathNode destNode = destination.node();
                        destination = new PathDestinationAbstract(failed.end()) {
                            @Override
                            public double destinationScore(@NotNull PathNode node) {
                                return destNode.distanceSquaredTo(node);
                            }
                        };
                        destinations.add(destination);
                    }
                }

                double g = calculator.computeG(context, currentNode, sample, destination);
                if(g < sample.score.g) {
                    sample.parent = currentNode;
                    openSet.update(sample, node -> node.score = new Score(g, calculator.computeH(context, sample, destination)));
                }

                //heuristic-only comparison for 'best path' in case of inaccessible target
                if(sample.score.h < bestFound.score.h) {
                    bestFound = sample;
                }
            }
        }
        else {
            throw new IllegalStateException("Cannot call step() for PathOperation with state " + state);
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
        return 1;
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
    public @NotNull PathAgent agent() {
        return agent;
    }

    @Override
    public @NotNull ChunkRange searchArea() {
        return range;
    }

    @Override
    public @NotNull NodeProvider nodeProvider() {
        return nodeProvider;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private void complete(boolean success, PathDestination destination) {
        state = success ? State.SUCCEEDED : State.FAILED;
        result = new PathResultImpl(firstNode, success ? currentNode : bestFound, this, visited, destination, state);
    }
}