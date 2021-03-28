package io.github.zap.arenaapi.pathfind;

class BlockPathDestination extends PathDestinationAbstract {
    BlockPathDestination(int x, int y, int z) {
        super(new PathNode(x, y, z));
    }
}
