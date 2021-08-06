package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector2I;
import io.github.zap.vector.Vectors;
import org.junit.jupiter.api.*;

public class ChunkBoundsTest {
    private ChunkBounds radialSquare;
    private ChunkBounds by2;

    @BeforeEach
    public void setUp() {
        radialSquare = (ChunkBounds)ChunkCoordinateProvider.squareFromCenter(Vectors.of(0, 0), 1);
        by2 = new ChunkBounds(0, 0, 1, 1);
    }

    @AfterEach
    public void tearDown() {
        radialSquare = null;
    }

    @Test
    public void testChunkCount() {
        Assertions.assertEquals(9, radialSquare.chunkCount());
        Assertions.assertEquals(1, by2.chunkCount());

        int rCount = 0;
        int by2count = 0;

        for(Vector2I cv : radialSquare) {
            rCount++;
        }

        for(Vector2I cv : by2) {
            by2count++;
        }

        Assertions.assertEquals(rCount, radialSquare.chunkCount());
        Assertions.assertEquals(by2count, by2.chunkCount());
    }
}
