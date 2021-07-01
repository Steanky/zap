package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompoundChunkCoordinateProvider implements ChunkCoordinateProvider {
    private final Set<ChunkVectorAccess> mergedCoordinates;

    public CompoundChunkCoordinateProvider() {
        mergedCoordinates = new HashSet<>();
    }

    public void union(@NotNull ChunkCoordinateProvider provider) {
        for(ChunkVectorAccess vectorAccess : provider) {
            mergedCoordinates.add(vectorAccess);
        }
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return mergedCoordinates.contains(ChunkVectorAccess.immutable(x, z));
    }

    @Override
    public int chunkCount() {
        return mergedCoordinates.size();
    }

    @NotNull
    @Override
    public Iterator<ChunkVectorAccess> iterator() {
        return mergedCoordinates.iterator();
    }
}
