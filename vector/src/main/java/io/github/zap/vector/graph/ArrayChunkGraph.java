package io.github.zap.vector.graph;

import org.jetbrains.annotations.Nullable;

/**
 * Array-based implementation of a chunk graph, which is a data structure that stores elements indexed by 3 integer keys.
 */
public class ArrayChunkGraph<T> implements ChunkGraph<T> {
    private final NodeChunk[][] chunkArray;

    private final int minX;
    private final int minZ;
    private final int width;
    private final int height;

    /**
     * Creates a new ArrayChunkGraph over the specified bounds. This will determine the ChunkGraph's initial capacity.
     * The maximum amount of storable elements is 65536 * width * height, although the effective size of the stored
     * array will increase as more components are allocated. The initial size of the array will simply be equal to
     * width * height.
     *
     * The region of values over which this array may be used is determined by the minX, minZ, maxX and maxZ parameters.
     * Specifically, one cannot store elements whose key would be outside of the specified bounds. More specifically,
     * the key's X and Z values cannot go below their respective min, nor can they equal or exceed their respective
     * max. If the key is invalid (out of bounds), an ArrayIndexOutOfBounds exception will be thrown.
     */
    public ArrayChunkGraph(int minX, int minZ, int maxX, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minZ = Math.min(minZ, maxZ);

        chunkArray = new NodeChunk[width = Math.abs(maxX - minX)][height = Math.abs(maxZ - minZ)];
    }

    @Override
    public @Nullable T elementAt(int x, int y, int z) {
        int indexX = (x >> 4) - minX;
        int indexZ = (z >> 4) - minZ;

        if(inRange(indexX, y, indexZ)) {
            NodeChunk nodeChunk = chunkArray[indexX][indexZ];

            if(nodeChunk != null) {
                NodeSegment segment = nodeChunk.get(y >> 4);

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
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }

        return null;
    }

    @Override
    public boolean removeElement(int x, int y, int z) {
        int indexX = (x >> 4) - minX;
        int indexZ = (z >> 4) - minZ;

        if(inRange(indexX, y, indexZ)) {
            NodeChunk nodeChunk = chunkArray[indexX][indexZ];

            if(nodeChunk != null) {
                NodeSegment segment = nodeChunk.get(y >> 4);

                if(segment != null) {
                    NodeLayer layer = segment.get(y & 15);

                    if(layer != null) {
                        NodeRow row = layer.get(x & 15);

                        if(row != null) {
                            row.set(z & 15, null);
                            return true;
                        }
                    }
                }
            }
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }

        return false;
    }

    @Override
    public boolean hasElement(int x, int y, int z) {
        return elementAt(x, y, z) != null;
    }

    @Override
    public void putElement(int x, int y, int z, @Nullable T node) {
        int indexX = (x >> 4) - minX;
        int indexZ = (z >> 4) - minZ;

        if(inRange(indexX, y, indexZ)) {
            NodeChunk nodeChunk = chunkArray[indexX][indexZ];
            nodeChunk = nodeChunk == null ? (chunkArray[indexX][indexZ] =
                    new NodeChunk(indexX, indexZ, (a, b) -> chunkArray[a][b] = null)) : nodeChunk;

            int segmentIndex = y >> 4;
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
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    private boolean inRange(int indexX, int y, int indexZ) {
        return indexX >= 0 && indexX < width && y >= 0 && y < 256 && indexZ >= 0 && indexZ < height;
    }
}
