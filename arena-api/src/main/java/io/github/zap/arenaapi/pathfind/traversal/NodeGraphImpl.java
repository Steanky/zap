package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.PathNode;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NodeGraphImpl implements NodeGraph {
    private final Map<ChunkVectorAccess, NodeChunk> chunks = new HashMap<>();

    @Override
    public @Nullable NodeLocation nodeAt(int x, int y, int z) {
        NodeChunk nodeChunk = chunks.get(ChunkVectorAccess.immutable(x >> 4, z >> 4));

        NodeSegment segment = nodeChunk.get(y >> 4);
        if(segment != null) {
            NodeLayer layer = segment.get(y & 15);

            if(layer != null) {
                NodeRow row = layer.get(x & 15);

                if(row != null) {
                    return row.get(z & 15);
                }
            }
        }

        return null;
    }

    @Override
    public void putNode(@NotNull PathNode node, @NotNull PathOperation operation) {
        int chunkX = node.nodeX() >> 4;
        int chunkZ = node.nodeZ() >> 4;
        NodeChunk nodeChunk = chunks.computeIfAbsent(ChunkVectorAccess.immutable(chunkX, chunkZ),
                (chunkVectorAccess -> new NodeChunk(this, chunkX, chunkZ)));

        int segmentIndex = node.nodeY() >> 4;
        int layerIndex = node.nodeY() & 15;
        int rowIndex = node.nodeX() & 15;
        int nodeIndex = node.nodeZ() & 15;

        NodeSegment segment = nodeChunk.get(segmentIndex);
        NodeLayer layer;
        NodeRow row;

        if(segment == null) {
            nodeChunk.set(segmentIndex, (segment = new NodeSegment(nodeChunk, segmentIndex)));
            segment.set(layerIndex, (layer = new NodeLayer(segment, layerIndex)));
            layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
            row.set(nodeIndex, new NodeLocation(row, node, operation, nodeIndex));
        }
        else {
            layer = segment.get(layerIndex);
            if(layer == null) {
                segment.set(layerIndex, (layer = new NodeLayer(segment, layerIndex)));
                layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
                row.set(nodeIndex, new NodeLocation(row, node, operation, nodeIndex));
            }
            else {
                row = layer.get(rowIndex);
                if(row == null) {
                    layer.set(rowIndex, (row = new NodeRow(layer, rowIndex)));
                    row.set(nodeIndex, new NodeLocation(row, node, operation, nodeIndex));
                }
                else {
                    row.set(nodeIndex, new NodeLocation(row, node, operation, nodeIndex));
                }
            }
        }
    }

    @Override
    public void removeNode(int x, int y, int z) {

    }

    @Override
    public void removeChunk(int chunkX, int chunkZ) {
        chunks.remove(ChunkVectorAccess.immutable(chunkX, chunkX));
    }

    @Override
    public boolean containsNode(int x, int y, int z) {
        return nodeAt(x, y, z) != null;
    }
}
