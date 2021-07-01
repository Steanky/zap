package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.ChunkVectorAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CompoundChunkCoordinateProvider implements ChunkCoordinateProvider {
    private static class ChunkVectorEntry {
        private final ChunkVectorAccess vectorAccess;
        private final ChunkCoordinateProvider linkedProvider;

        private ChunkVectorEntry(@NotNull ChunkVectorAccess vectorAccess,
                                 @Nullable ChunkCoordinateProvider linkedProvider) {
            this.vectorAccess = vectorAccess;
            this.linkedProvider = linkedProvider;
        }

        @Override
        public int hashCode() {
            return vectorAccess.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ChunkVectorEntry) {
                ChunkVectorEntry other = (ChunkVectorEntry)obj;
                return other.vectorAccess.equals(this.vectorAccess);
            }

            return false;
        }
    }

    private final Map<ChunkVectorEntry, ChunkVectorEntry> mergedCoordinates;

    public CompoundChunkCoordinateProvider() {
        mergedCoordinates = new HashMap<>();
    }

    public void union(@NotNull ChunkCoordinateProvider provider) {
        for(ChunkVectorAccess vectorAccess : provider) {
            ChunkVectorEntry entry = new ChunkVectorEntry(vectorAccess, provider);
            mergedCoordinates.put(entry, entry);
        }
    }

    @Override
    public boolean hasChunk(int x, int z) {
        ChunkVectorEntry entry = mergedCoordinates.get(new ChunkVectorEntry(ChunkVectorAccess.immutable(x, z), null));
        if(entry != null && entry.linkedProvider != null) {
            return entry.linkedProvider.hasChunk(x, z);
        }

        return false;
    }

    @Override
    public int chunkCount() {
        return mergedCoordinates.size();
    }

    @NotNull
    @Override
    public Iterator<ChunkVectorAccess> iterator() {
        return mergedCoordinates.values().stream().map(t -> t.vectorAccess).iterator();
    }
}
