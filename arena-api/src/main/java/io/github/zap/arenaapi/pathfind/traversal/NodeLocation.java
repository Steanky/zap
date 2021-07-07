package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import io.github.zap.arenaapi.pathfind.PathOperation;

record NodeLocation(NodeRow parent, PathNode node, PathOperation operation, int parentIndex) { }
