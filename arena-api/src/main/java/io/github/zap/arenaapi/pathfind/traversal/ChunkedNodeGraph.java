package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChunkedNodeGraph implements NodeGraph {
    private final Map<ChunkVectorAccess, NodeChunk> chunks = new HashMap<>();

    @Override
    public @Nullable NodeLocation nodeAt(int x, int y, int z) {
        NodeChunk nodeChunk = chunks.get(ChunkVectorAccess.immutable(x >> 4, z >> 4));

        if(nodeChunk != null) {
            NodeSegment segment = nodeChunk.get((y >> 4) & 15);

            if(segment != null) {
                NodeLayer layer = segment.get(y & 15);

                if(layer != null) {
                    NodeRow row = layer.get(x & 15);

                    if(row != null) {
                        return row.get(z & 15);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void chainNode(int x, int y, int z, @NotNull PathNode parent, @NotNull PathOperation operation) {
        PathNode newNode = new PathNode(x, y, z);
        newNode.chain(parent);

        putNodeInternal(x, y, z, newNode, operation);
    }

    @Override
    public void putNode(@NotNull PathNode node, @NotNull PathOperation operation) {
        putNodeInternal(node.nodeX(), node.nodeY(), node.nodeZ(), node, operation);
    }

    @Override
    public void removeNode(int x, int y, int z) {
        putNodeInternal(x, y, z, null, null);
    }

    @Override
    public void removeChunk(int chunkX, int chunkZ) {
        chunks.remove(ChunkVectorAccess.immutable(chunkX, chunkX));
    }

    @Override
    public boolean containsNode(int x, int y, int z) {
        return nodeAt(x, y, z) != null;
    }

    @Override
    public boolean containsNode(@NotNull PathNode node) {
        return nodeAt(node.nodeX(), node.nodeY(), node.nodeZ()) != null;
    }

    private void putNodeInternal(int x, int y, int z, @Nullable PathNode node, @Nullable PathOperation operation) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        NodeChunk nodeChunk = chunks.computeIfAbsent(ChunkVectorAccess.immutable(chunkX, chunkZ),
                (chunkVectorAccess -> new NodeChunk(this, chunkX, chunkZ)));

        int segmentIndex = (y >> 4) & 15;
        int layerIndex = y & 15;
        int rowIndex = x & 15;
        int nodeIndex = z & 15;

        NodeSegment segment = nodeChunk.get(segmentIndex);
        NodeLayer layer;
        NodeRow row;

        if(segment == null) {
            nodeChunk.set(segmentIndex, (segment = new NodeSegment(nodeChunk, segmentIndex)));
            segment.set(layerIndex, (layer = new NodeLayer(segment, layerIndex)));
            layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
        }
        else {
            layer = segment.get(layerIndex);
            if(layer == null) {
                segment.set(layerIndex, (layer = new NodeLayer(segment, layerIndex)));
                layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
            }
            else {
                row = layer.get(rowIndex);
                if(row == null) {
                    layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
                }
            }
        }

        row.set(nodeIndex, (node == null || operation == null) ? null : new NodeLocation(row, node, operation, nodeIndex));
    }
}
