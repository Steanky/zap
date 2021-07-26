package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChunkBoundsTest {
    private ChunkBounds radialSquare;
    private ChunkBounds by2;

    @Before
    public void setUp() {
        radialSquare = (ChunkBounds)ChunkCoordinateProvider.squareFromCenter(Vectors.of(0, 0), 1);
        by2 = new ChunkBounds(0, 0, 1, 1);
    }

    @After
    public void tearDown() {
        radialSquare = null;
    }

    @Test
    public void testChunkCount() {
        Assert.assertEquals(9, radialSquare.chunkCount());
        Assert.assertEquals(1, by2.chunkCount());

        int rCount = 0;
        int by2count = 0;

        for(Vector2I cv : radialSquare) {
            rCount++;
        }

        for(Vector2I cv : by2) {
            by2count++;
        }

        Assert.assertEquals(rCount, radialSquare.chunkCount());
        Assert.assertEquals(by2count, by2.chunkCount());
    }
}