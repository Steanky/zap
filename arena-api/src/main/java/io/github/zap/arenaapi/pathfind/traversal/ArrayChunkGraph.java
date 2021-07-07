package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ArrayChunkGraph<T> implements ChunkGraph<T> {
    private final Map<ChunkVectorAccess, NodeChunk> chunks = new HashMap<>();

    @Override
    public @Nullable T elementAt(int x, int y, int z) {
        NodeChunk nodeChunk = chunks.get(ChunkVectorAccess.immutable(x >> 4, z >> 4));

        if(nodeChunk != null) {
            NodeSegment segment = nodeChunk.get((y >> 4) & 15);

            if(segment != null) {
                NodeLayer layer = segment.get(y & 15);

                if(layer != null) {
                    NodeRow row = layer.get(x & 15);

                    if(row != null) {
                        NodeLocation nodeLocation = row.get(z & 15);
                        //noinspection unchecked
                        return nodeLocation == null ? null : (T)nodeLocation.node();
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void removeElement(int x, int y, int z) {
        putElement(x, y, z, null);
    }

    @Override
    public boolean hasElement(int x, int y, int z) {
        return elementAt(x, y, z) != null;
    }

    @Override
    public void putElement(int x, int y, int z, @Nullable T node) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        NodeChunk nodeChunk = chunks.computeIfAbsent(ChunkVectorAccess.immutable(chunkX, chunkZ),
                (chunkVectorAccess -> new NodeChunk( chunkX, chunkZ, (a, b) -> chunks.remove(ChunkVectorAccess.immutable(a, b)))));

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

        row.set(nodeIndex, node == null ? null : new NodeLocation(row, node, nodeIndex));
    }
}
