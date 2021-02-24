package io.github.zap.arenaapi.util;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents a subsection of an array. This subsection can be iterated and indexed, just like a regular array. Since
 * this does not create a new array, any changes made to the segment will be reflected in the original. Otherwise, this
 * object is immutable. Indices are relative to the offset; in other words, get(0) will return array[offset].
 */
public class ArraySegment<T> implements Iterable<T> {
    private class SegmentIterator implements Iterator<T> {
        private int i = -1;

        @Override
        public boolean hasNext() {
            return i + 1 < length;
        }

        @Override
        public T next() {
            return array[offset + ++i];
        }
    }

    private final T[] array;
    private final int offset;

    @Getter
    private final int length;

    /**
     * Creates a new ArraySegment object from the specified array, length, and offset. An IndexOutOfBounds exception
     * will be thrown if fully iterating the ArraySegment would at any point cause an ArrayIndexOutOfBounds exception.
     * @param array The array to make a segment from
     * @param length The length of the segment
     * @param offset The offset (starting index) of this segment
     */
    public ArraySegment(T[] array, int length, int offset) {
        Objects.requireNonNull(array, "array cannot be null");

        if(length < 0) {
            throw new ArrayIndexOutOfBoundsException("length cannot be negative");
        }

        if(length > array.length) {
            throw new ArrayIndexOutOfBoundsException("length cannot be greater than the length of the underlying array");
        }

        if(offset < 0 || offset > array.length) {
            throw new ArrayIndexOutOfBoundsException("offset must be within the bounds of the array");
        }

        if(offset + length > array.length) {
            throw new ArrayIndexOutOfBoundsException("part of the segment is out of bounds for the array");
        }

        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Creates an ArraySegment encompassing the entire array.
     * @param array The array to effectively wrap
     */
    public ArraySegment(T[] array) {
        this(array, array.length, 0);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new SegmentIterator();
    }

    /**
     * Retrieves the object at index i. An ArrayIndexOutOfBoundsException will be thrown if is out of bounds for the
     * underlying array, or if it is outside the region managed by this ArraySegment.
     * @param i The index, relative to offset for the underlying array
     * @return The array element
     */
    public T get(int i) {
        if(i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException("array index out of bounds");
        }

        return array[offset + i];
    }

    /**
     * Sets the object at index i for the underlying array. An ArrayIndexOutOfBoundsException will be thrown if is out
     * of bounds for the underlying array, or if it is outside the region managed by this ArraySegment.
     * @param i The relative index
     * @param value The object to assign to that index
     */
    public void set(int i, T value) {
        if(i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException("array index out of bounds");
        }

        array[offset + i] = value;
    }

    /**
     * Creates a copy of the segment of the original array managed by this ArraySegment.
     * @return A new array which is copied from the underlying array, containing all the elements managed by this
     * ArraySegment
     */
    public T[] copyOf() {
        Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length);

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, offset, newArray, 0, length);

        //noinspection unchecked
        return (T[]) newArray;
    }

    /**
     * Wrapped for System.arraycopy that copies this ArraySegment's values to another array.
     * @param offset The offset relative to the start of this ArraySegment
     * @param dest The destination array object
     * @param destOffset The offset of the destination array
     * @param length The number of copied elements
     */
    public void copyTo(int offset, Object dest, int destOffset, int length) {
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, this.offset + offset, dest, destOffset, length);
    }
}