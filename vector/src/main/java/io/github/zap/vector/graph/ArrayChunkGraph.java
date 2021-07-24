package io.github.zap.vector.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Array-based implementation of a chunk graph, which is a data structure that stores elements indexed by 3 integer keys.
 */
public class ArrayChunkGraph<T> implements ChunkGraph<T> {
    private class ArrayChunkGraphIterator implements Iterator<T> {
        private static record Indices(int x, int z, int s, int l, int r, int n, NodeLocation cached) {}

        private int x = 0;
        private int z = -1;
        private int s = -1;
        private int l = -1;
        private int r = -1;
        private int n = -1;

        private NodeChunk chunk;
        private NodeSegment segment;
        private NodeLayer layer;
        private NodeRow row;
        private NodeLocation node;

        private Indices cachedIndices; //so we don't have to iterate twice

        @Override
        public boolean hasNext() { //hasNext should at least be idempotent even if it isn't technically stateless
            return nextInternal() != null;
        }

        @Override
        public T next() {
            if(cachedIndices != null) {
                updateIndices();

                //noinspection unchecked
                T value = (T)cachedIndices.cached.node();
                cachedIndices = null;
                return value;
            }
            else {
                NodeLocation next = nextInternal();

                if(next == null) {
                    throw new IllegalStateException("Iterator has no more elements!");
                }

                cachedIndices = null;

                //noinspection unchecked
                return (T)next.node();
            }
        }

        @Override
        public void remove() {
            if(node == null) {
                throw new IllegalStateException("next() must be called once for each remove");
            }

            cachedIndices = null;

            node.remove();
            node = null;
        }

        private NodeLocation nextInternal() {
            if(cachedIndices != null) {
                return cachedIndices.cached;
            }

            int x = this.x;
            int z = this.z;

            int s = this.s;
            int l = this.l;

            int r = this.r;
            int n = this.n;

            n++;
            if(row == null || !row.hasNonNull(n)) {
                r++;

                if(layer == null || !layer.hasNonNull(r)) {
                    l++;

                    if(segment == null || !segment.hasNonNull(l)) {
                        s++;

                        if(chunk == null || !chunk.hasNonNull(s)) {
                            boolean foundChunk = false;

                            z++;
                            outer:
                            for(int i = x; i < width; i++) {
                                for(int j = z; j < height; j++) {
                                    NodeChunk chunk = chunkArray[i][j];

                                    if(chunk != null) {
                                        x = i;
                                        z = j;

                                        foundChunk = true;
                                        this.chunk = chunk;
                                        break outer;
                                    }
                                }

                                z = 0;
                            }

                            if(!foundChunk) {
                                return null;
                            }

                            s = 0;
                        }

                        ArrayContainer.Entry<NodeSegment> nextSegment = chunk.firstNonNull(s);
                        s = nextSegment.index();
                        segment = nextSegment.element();

                        l = 0;
                    }

                    ArrayContainer.Entry<NodeLayer> nextLayer = segment.firstNonNull(l);
                    l = nextLayer.index();
                    layer = nextLayer.element();

                    r = 0;
                }

                ArrayContainer.Entry<NodeRow> nextRow = layer.firstNonNull(r);
                r = nextRow.index();
                row = nextRow.element();

                n = 0;
            }

            ArrayContainer.Entry<NodeLocation> nextNode = row.firstNonNull(n);
            n = nextNode.index();
            node = nextNode.element();

            cachedIndices = new Indices(x, z, s, l, r, n, node);
            return node;
        }

        private void updateIndices() {
            this.x = cachedIndices.x;
            this.z = cachedIndices.z;
            this.s = cachedIndices.s;
            this.l = cachedIndices.l;
            this.r = cachedIndices.r;
            this.n = cachedIndices.n;
        }
    }

    private final NodeChunk[][] chunkArray;

    private final int width;
    private final int height;

    private final int minX;
    private final int minZ;

    private int size;

    /**
     * Creates a new ArrayChunkGraph over the specified chunk bounds. This will determine the ChunkGraph's initial capacity.
     * The maximum amount of storable elements is 65536 * width * height, although the actual size in memory of the
     * array will increase as more components are allocated. The initial size of the array will simply be equal to
     * width * height.
     *
     * The region of values over which this array may be used is determined by the minX, minZ, maxX and maxZ parameters.
     * Specifically, one cannot store elements whose key would be outside of the specified bounds. More specifically,
     * the key's X and Z values cannot go below their respective min, nor can they equal or exceed their respective
     * max. If the key is invalid (out of bounds), an ArrayIndexOutOfBounds exception will be thrown.
     *
     * For accessing values using triplets of integers, this collection is consistently 2-3 times faster than an
     * equivalent hashmap-based setup.
     */
    public ArrayChunkGraph(int minX, int minZ, int maxX, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minZ = Math.min(minZ, maxZ);

        maxX = Math.max(minX, maxX);
        maxZ = Math.max(minZ, maxZ);

        chunkArray = new NodeChunk[(width = maxX - this.minX)][(height = maxZ - this.minZ)];
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
            throw new ArrayIndexOutOfBoundsException("Key at x=" + x + ", y=" + y + ", z=" + z + " out of bounds for elementAt");
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
                            size--;
                            return true;
                        }
                    }
                }
            }
        }
        else {
            throw new ArrayIndexOutOfBoundsException("Key at x=" + x + ", y=" + y + ", z=" + z + " out of bounds for removeElement");
        }

        return false;
    }

    @Override
    public boolean hasElementAt(int x, int y, int z) {
        return inRange((x >> 4) - minX, y, (z >> 4) - minZ) && elementAt(x, y, z) != null;
    }

    @Override
    public int size() {
        return size;
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

            if(row.get(nodeIndex) == null) {
                size++;
            }

            row.set(nodeIndex, node == null ? null : new NodeLocation(row, node, nodeIndex));
        }
        else {
            throw new ArrayIndexOutOfBoundsException("Key at x=" + x + ", y=" + y + ", z=" + z + " out of bounds for putElement");
        }
    }

    private boolean inRange(int indexX, int y, int indexZ) {
        return indexX >= 0 && indexX < width && y >= 0 && y < 256 && indexZ >= 0 && indexZ < height;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new ArrayChunkGraphIterator();
    }
}
