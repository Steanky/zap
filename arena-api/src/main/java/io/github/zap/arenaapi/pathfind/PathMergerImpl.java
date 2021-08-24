package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.graph.ChunkGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PathMergerImpl implements PathMerger {
    @Override
    public @Nullable PathResult attemptMerge(@NotNull PathOperation operation, @NotNull PathfinderContext context) {
        PathNode currentNode = operation.currentNode();

        if(currentNode != null) {
            for(PathResult successful : context.successfulPaths()) {
                ChunkGraph<PathNode> resultVisited = successful.visitedNodes(); //get nodes visited by another path

                int x = currentNode.x();
                int y = currentNode.y();
                int z = currentNode.z();

                //check if we can merge
                if(operation.mergeValid(successful.operation()) && resultVisited.hasElementAt(x, y, z)) {
                    PathNode intersection = resultVisited.elementAt(x, y, z); //get intersection point

                    PathNode sample = intersection;
                    while(sample != null) {
                        PathNode parent = sample.parent; //iterate up parents

                        if(parent != null && parent.child != sample) { //check for "broken" link (indicative of path)
                            PathNode oldIntersectionChild = intersection.child;

                            intersection.child = currentNode.child;
                            if(currentNode.child != null) {
                                currentNode.child.parent = intersection; //link up paths
                            }

                            parent.child = sample; //point path back towards origin

                            PathNode first = intersection;
                            while(first.child != null) { //get origin node
                                first = first.child;
                            }


                            return new PathResultImpl(first, operation, resultVisited, operation.destination(),
                                    PathOperation.State.SUCCEEDED);
                        }

                        sample = sample.parent;
                    }

                    if(intersection != null) {
                        //if we reach this point, it means we started directly on an existing path
                        return new PathResultImpl(intersection, operation, resultVisited, operation.destination(),
                                PathOperation.State.SUCCEEDED);
                    }
                }
            }
        }

        return null;
    }
}
