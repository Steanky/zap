package io.github.zap.arenaapi.pathfind.chunk;

import com.google.common.collect.ImmutableSet;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

class ChunkBoundsIteratorTest {
    private static final Set<Vector2I> EXPECTED = ImmutableSet.of(Vectors.of(-1, -1), Vectors.of(-1, 0),
            Vectors.of(0, -1), Vectors.of(0, 0));

    private ChunkBoundsIterator iterator;

    @BeforeEach
    void setUp() {
        iterator = new ChunkBoundsIterator(new BoundingBox(-1, 0, -1, 1, 1, 1));
    }

    @Test
    void testSize() {
        int count = 0;
        while (iterator.hasNext()){
            iterator.next();
            count++;
        }

        Assertions.assertEquals(EXPECTED.size(), count);
    }

    @Test
    void testActualValues() {
        Set<Vector2I> observed = new HashSet<>();
        while(iterator.hasNext()) {
            observed.add(iterator.next());
        }

        Assertions.assertEquals(EXPECTED.size(), observed.size());
        for(Vector2I sample : observed) {
            Assertions.assertTrue(EXPECTED.contains(sample));
        }
    }

    @Test
    void testRedundantHasNext() {
        while(iterator.hasNext()) {
            //a poorly made iterator may not have an idempotent hasNext so test this
            //noinspection ConstantConditions
            Assertions.assertTrue(iterator.hasNext());
            Vector2I value = iterator.next();
            Assertions.assertNotNull(value);
            Assertions.assertTrue(EXPECTED.contains(value));
        }
    }

    @Test
    void testUncheckedNext() {
        for(int i = 0; i < EXPECTED.size(); i++) {
            iterator.next();
        }
    }

    @Test
    void testOverflowNext() {
        for(int i = 0; i < EXPECTED.size() + 1; i++) {
            if(i == EXPECTED.size()) {
                Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
            }
            else {
                iterator.next();
            }
        }
    }
}