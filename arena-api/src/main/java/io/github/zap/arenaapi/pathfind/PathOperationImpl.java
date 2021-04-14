package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private static final double MAX_MERGE_ANGLE = Math.PI / 6D;

    private final PathAgent agent;
    private final Set<PathDestination> destinations;
    private State state;
    private final ScoreCalculator calculator;
    private final SuccessCondition condition;
    private final NodeProvider nodeProvider;
    private final DestinationSelector destinationSelector;
    private final ChunkCoordinateProvider searchArea;

    //TODO: maintain separate NodeQueue for lower-priority nodes and only take from that queue when needed
    private final NodeQueue openSet = new BinaryHeapNodeQueue(128);
    private final Map<PathNode, PathNode> visited = new HashMap<>();
    private final Set<PathResult> consideredResults = new HashSet<>();
    private PathDestination destination;
    private PathNode currentNode;
    private PathNode bestFound;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<PathDestination> destinations,
                      @NotNull ScoreCalculator calculator, @NotNull SuccessCondition condition,
                      @NotNull NodeProvider nodeProvider, @NotNull DestinationSelector destinationSelector,
                      @NotNull ChunkCoordinateProvider searchArea) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.NOT_STARTED;
        this.calculator = calculator;
        this.condition = condition;
        this.nodeProvider = nodeProvider;
        this.destinationSelector = destinationSelector;
        this.searchArea = searchArea;
    }

    @Override
    public void init() {
        if(state == State.NOT_STARTED) {
            state = State.STARTED;
        }
        else {
            throw new IllegalStateException("Cannot initialize a PathOperation with state " + state);
        }
    }

    @Override
    public boolean allowMerge(@NotNull PathOperation other) {
        return other != this && nodeProvider.equals(other.nodeProvider()) && agent.equals(other.agent());
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.STARTED) {
            if(currentNode != null) {
                PathDestination best = null;
                double bestScore = Double.POSITIVE_INFINITY;
                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        complete(true, destination);
                        return true;
                    }

                    double score = calculator.computeH(context, currentNode, destination);
                    if(best == null || score < bestScore) {
                        best = destination;
                        bestScore = score;
                    }
                }

                if(best == null) {
                    complete(false, PathDestination.fromSource(currentNode.position()));
                    return true;
                }

                if(openSet.size() > 0) {
                    currentNode = openSet.takeBest();
                }
                else {
                    complete(false, destination == null ? best : destination);
                    return true;
                }
            }
            else {
                currentNode = new PathNode(null, agent);
                destination = destinationSelector.selectDestinationFor(this, currentNode);
                currentNode.score.set(0, calculator.computeH(context, currentNode, destination));
                bestFound = currentNode.copy();
            }

            visited.put(currentNode, currentNode);

            PathNode[] possibleNodes = nodeProvider.generateNodes(context, currentNode);
            for(PathNode sample : possibleNodes) {
                if(sample == null) {
                    break;
                }

                if(visited.containsKey(sample)) {
                    continue;
                }

                if(nodeProvider.mayTraverse(context, agent, currentNode, sample)) {
                    destination = destinationSelector.selectDestinationFor(this, sample);

                    //remove the unreachable destination so we don't expand a ton of nodes
                    for(PathResult failed : context.failedPaths()) {
                        if(!consideredResults.contains(failed)) {
                            if(destinationComparable(context, failed, sample, true)) {
                                PathNode connectionPoint = failed.visitedNodes().get(sample);
                                PathNode start = currentNode.reverse();
                                currentNode.parent = connectionPoint;

                                state = State.SUCCEEDED;
                                result = new PathResultImpl(start, this, visited, destination, state);
                                return true;
                            }

                            consideredResults.add(failed);
                        }
                    }

                    for(PathResult succeeded : context.successfulPaths()) {
                        if(!consideredResults.contains(succeeded)) {
                            if(destinationComparable(context, succeeded, sample, false)) {
                                PathNode connectionPoint = succeeded.visitedNodes().get(sample);
                                PathNode start = currentNode.reverse();
                                currentNode.parent = connectionPoint;

                                state = State.SUCCEEDED;
                                result = new PathResultImpl(start, this, visited, destination, state);
                                return true;
                            }

                            consideredResults.add(succeeded);
                        }
                    }

                    double g = calculator.computeG(context, currentNode, sample, destination);
                    if(g < sample.score.getG()) {
                        PathNode newSample = sample.copy();
                        newSample.score.set(g, calculator.computeH(context, sample, destination));
                        openSet.replaceNode(sample, newSample);
                        sample = newSample;
                    }

                    //comparison for best path in case of inaccessible target
                    if(sample.score.getF() < bestFound.score.getF()) {
                        bestFound = sample.copy();
                    }
                }
            }
        }
        else {
            throw new IllegalStateException("Cannot call step() for PathOperation with state " + state);
        }

        return false;
    }

    @Override
    public @NotNull PathOperation.State state() {
        return state;
    }

    @Override
    public @NotNull PathResult result() {
        if(state == State.STARTED) {
            throw new IllegalStateException("Cannot get PathResult for a PathOperation that has not completed!");
        }

        if(result == null) {
            throw new IllegalStateException("Result has not been compiled for " + this);
        }

        return result;
    }

    @Override
    public int iterations() {
        return 100;
    }

    @Override
    public @NotNull Set<PathDestination> getDestinations() {
        return destinations;
    }

    @Override
    public @NotNull Map<PathNode, PathNode> visitedNodes() {
        return visited;
    }

    @Override
    public @NotNull PathAgent agent() {
        return agent;
    }

    @Override
    public @NotNull ChunkCoordinateProvider searchArea() {
        return searchArea;
    }

    @Override
    public @NotNull NodeProvider nodeProvider() {
        return nodeProvider;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private boolean validateAngle(PathNode first, PathNode second) {
        double b = first.position().distance(destination.position());
        double a = second.position().distance(destination.position());
        double c = first.position().distanceSquared(second.position());

        return Math.acos(((a * a) + (b * b) - c) / (2 * a * b)) <= MAX_MERGE_ANGLE;
    }

    private boolean destinationComparable(PathfinderContext context, PathResult result, PathNode walkTo, boolean checkInverseWalkability) {
        return !result.destination().equals(this.destination) ||
                !result.operation().nodeProvider().equals(nodeProvider) ||
                !result.operation().agent().characteristics().equals(agent.characteristics()) ||
                !result.visitedNodes().containsKey(walkTo) ||
                !checkInverseWalkability || result.operation().nodeProvider().mayTraverse(context, agent, walkTo, currentNode);
    }

    private void complete(boolean success, PathDestination destination) {
        state = success ? State.SUCCEEDED : State.FAILED;
        result = new PathResultImpl(success ? currentNode.reverse() : bestFound.reverse(), this, visited, destination, state);
    }
}