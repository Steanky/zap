package io.github.zap.vector.util;

import io.github.zap.vector.ImmutableWorldVector;
import io.github.zap.vector.VectorAccess;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VectorHelperTest {
    List<VectorAccess> worldRelative;
    List<VectorAccess> chunkRelative;
    List<Integer> chunkX;
    List<Integer> chunkZ;

    @Before
    public void setUp() {
        worldRelative = new ArrayList<>();
        chunkRelative = new ArrayList<>();
        chunkX = new ArrayList<>();
        chunkZ = new ArrayList<>();

        for(int i = -100; i < 100; i++) {
            for(int j = -100; j < 100; j++) {
                worldRelative.add(VectorAccess.immutable(i, 0, j));
                chunkRelative.add(VectorHelper.toChunkRelative(worldRelative.get(worldRelative.size() - 1)));
            }
        }
    }

    @After
    public void tearDown() {
        worldRelative = null;
        chunkRelative = null;
        chunkX = null;
        chunkZ = null;
    }

    @Test
    public void toChunkRelative() {
        for(VectorAccess vectorAccess : worldRelative) {
            System.out.print(vectorAccess + " [Chunk relative] --> ");
            VectorAccess before = vectorAccess.copyVector();
            ImmutableWorldVector relative = VectorHelper.toChunkRelative(vectorAccess);
            Assert.assertEquals(before, vectorAccess);
            System.out.println(relative);

            Assert.assertTrue(validChunkRelative(relative.blockX(), relative.blockY(), relative.blockZ()));
        }
    }

    @Test
    public void toWorldRelative() {
        int chunkX = -20000;
        int chunkZ = -20000;
        for(VectorAccess vectorAccess : chunkRelative) {
            VectorAccess before = vectorAccess.copyVector();
            VectorAccess world = VectorHelper.toWorldRelative(vectorAccess, ++chunkX, ++chunkZ);
            Assert.assertEquals(before, vectorAccess);
            Assert.assertEquals(world.blockX(), ((long) chunkX << 4) + vectorAccess.blockX());
            Assert.assertEquals(world.blockZ(), ((long) chunkZ << 4) + vectorAccess.blockZ());
        }
    }

    private boolean validChunkRelative(int x, int y, int z) {
        return x >= 0 && x < 16 && y >= 0 && y < 256 && z >= 0 && z < 16;
    }
}