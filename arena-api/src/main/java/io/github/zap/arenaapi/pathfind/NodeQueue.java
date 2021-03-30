package io.github.zap.arenaapi.pathfind;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

class NodeQueue {
    private static final Comparator<PathNode> COMPARATOR = NodeComparator.instance();
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private PathNode[] queue;
    private int size;

    public NodeQueue() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public NodeQueue(int initialCapacity) {
        this.queue = new PathNode[initialCapacity];
    }

    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                (oldCapacity + 2) :
                (oldCapacity >> 1));
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        queue = Arrays.copyOf(queue, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public boolean add(PathNode e) {
        return offer(e);
    }

    public boolean offer(PathNode e) {
        if (e == null)
            throw new NullPointerException();
        int i = size;
        if (i >= queue.length)
            grow(i + 1);
        siftUp(i, e);
        size = i + 1;
        return true;
    }

    public void update(PathNode original, PathNode newValue) {
        int comparisonResult = COMPARATOR.compare(original, newValue);

        if(comparisonResult < 0) {
            siftUp(indexOf(original), newValue);
        }
        else if(comparisonResult > 0) {
            siftDown(indexOf(original), newValue);
        }
    }

    public PathNode peek() {
        return queue[0];
    }

    private int indexOf(PathNode o) {
        if (o != null) {
            for (int i = 0, n = size; i < n; i++)
                if (o.equals(queue[i]))
                    return i;
        }
        return -1;
    }

    public boolean remove(PathNode o) {
        int i = indexOf(o);
        if (i == -1)
            return false;
        else {
            removeAt(i);
            return true;
        }
    }

    void removeEq(Object o) {
        for (int i = 0, n = size; i < n; i++) {
            if (o == queue[i]) {
                removeAt(i);
                break;
            }
        }
    }

    public boolean contains(PathNode o) {
        return indexOf(o) >= 0;
    }

    public Iterator<PathNode> iterator() {
        return new NodeQueue.Itr();
    }

    private final class Itr implements Iterator<PathNode> {
        private int cursor;
        private int lastRet = -1;
        private ArrayDeque<PathNode> forgetMeNot;
        private PathNode lastRetElt;
        Itr() {}

        public boolean hasNext() {
            return cursor < size ||
                    (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        public PathNode next() {
            if (cursor < size)
                return queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (lastRet != -1) {
                PathNode moved = NodeQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                NodeQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0, n = size; i < n; i++)
            queue[i] = null;
        size = 0;
    }

    public PathNode poll() {
        final PathNode result;

        if ((result = queue[0]) != null) {
            final int n;
            final PathNode x = queue[(n = --size)];
            queue[n] = null;
            if (n > 0) {
                siftDown(0, x);
            }
        }
        return result;
    }

    PathNode removeAt(int i) {
        int s = --size;
        if (s == i)
            queue[i] = null;
        else {
            PathNode moved = queue[s];
            queue[s] = null;
            siftDown(i, moved);
            if (queue[i] == moved) {
                siftUp(i, moved);
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    private void siftUp(int k, PathNode x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            PathNode e = queue[parent];
            if (COMPARATOR.compare(x, e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    private void siftDown(int k, PathNode x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            PathNode c = queue[child];
            int right = child + 1;
            if (right < size && COMPARATOR.compare(c, queue[right]) > 0)
                c = queue[child = right];
            if (COMPARATOR.compare(x, c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    private void heapify() {
        int n = size, i = (n >>> 1) - 1;
        for (; i >= 0; i--)
            siftDown(i, queue[i]);
    }

    public Comparator<PathNode> comparator() {
        return COMPARATOR;
    }

    public final Spliterator<PathNode> spliterator() {
        return new PathNodeSpliterator(0, -1);
    }

    private final class PathNodeSpliterator implements Spliterator<PathNode> {
        private int index;
        private int fence;

        PathNodeSpliterator(int origin, int fence) {
            this.index = origin;
            this.fence = fence;
        }

        private int getFence() {
            int hi;
            if ((hi = fence) < 0) {
                hi = fence = size;
            }
            return hi;
        }

        public PathNodeSpliterator trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : new PathNodeSpliterator(lo, index = mid);
        }

        public void forEachRemaining(Consumer<? super PathNode> action) {
            if (action == null)
                throw new NullPointerException();
            if (fence < 0) { fence = size; }
            int i, hi; PathNode e;
            for (i = index, index = hi = fence; i < hi; i++) {
                if ((e = queue[i]) == null)
                    break;
                action.accept(e);
            }
        }

        public boolean tryAdvance(Consumer<? super PathNode> action) {
            if (action == null)
                throw new NullPointerException();
            if (fence < 0) { fence = size; }
            int i;
            if ((i = index) < fence) {
                index = i + 1;
                action.accept(queue[i]);
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return getFence() - index;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }

    public boolean removeIf(Predicate<? super PathNode> filter) {
        Objects.requireNonNull(filter);
        return bulkRemove(filter);
    }

    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return bulkRemove(c::contains);
    }

    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return bulkRemove(e -> !c.contains(e));
    }

    private static long[] nBits(int n) {
        return new long[((n - 1) >> 6) + 1];
    }
    private static void setBit(long[] bits, int i) {
        bits[i >> 6] |= 1L << i;
    }
    private static boolean isClear(long[] bits, int i) {
        return (bits[i >> 6] & (1L << i)) == 0;
    }

    private boolean bulkRemove(Predicate<? super PathNode> filter) {
        final int end = size;
        int i;
        for (i = 0; i < end && !filter.test(queue[i]); i++);
        if (i >= end) {
            return false;
        }
        final int beg = i;
        final long[] deathRow = nBits(end - beg);
        deathRow[0] = 1L;
        for (i = beg + 1; i < end; i++)
            if (filter.test(queue[i]))
                setBit(deathRow, i - beg);
        int w = beg;
        for (i = beg; i < end; i++)
            if (isClear(deathRow, i - beg))
                queue[w++] = queue[i];
        for (i = size = w; i < end; i++)
            queue[i] = null;
        heapify();
        return true;
    }

    public void forEach(Consumer<? super PathNode> action) {
        Objects.requireNonNull(action);
        for (int i = 0, n = size; i < n; i++)
            action.accept(queue[i]);
    }
}
