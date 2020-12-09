package io.github.zap.arenaapi.playerdata;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.*;

public class FileDataManager implements PlayerDataManager {
    @Getter
    private final File playerFile;

    @Getter
    private final DataLoader loader;

    @Getter
    private final int memoryCacheLength;

    private final Map<UUID, FilePlayerData> cache = new LinkedHashMap<>();
    private final Set<UUID> cacheKeys = cache.keySet();

    public FileDataManager(File playerFile, DataLoader loader, int memoryCacheLength) {
        this.playerFile = playerFile;
        this.loader = loader;
        this.memoryCacheLength = memoryCacheLength;
    }

    @Override
    public PlayerData getPlayerData(UUID id) {
        FilePlayerData data = cache.get(id);

        if(data != null) { //data was cached so just retrieve it
            return data;
        }


        String name = id.toString();
        try { //data was not cached; try to load the playerdata from a file
            data = loader.load(playerFile, name);
        }
        catch (ClassCastException e) { //there's some kind of invalid data there
            ArenaApi.warning(String.format("Tried to load non-PlayerData object at name %s in the playerdata file",
                    name));
            return null;
        }

        if(data != null) { //data was stored in the file, cache it in case of further use
            addMapping(id, data);
            return data;
        }
        else { //data did not exist in our file; create a new empty entry for it
            FilePlayerData newData = new FilePlayerData();
            addMapping(id, newData);
            return newData;
        }
    }

    private void addMapping(UUID id, FilePlayerData data) {
        cache.put(id, data); //add the mapping to the cache

        if(cacheKeys.size() > memoryCacheLength) { //if we exceeded the limit, we must remove the first entry
            UUID remove = null;
            for(UUID uuid : cacheKeys) {
                remove = uuid;
                break;
            }

            FilePlayerData removedValue = cache.get(remove); //get the value so we can save it if needed
            cacheKeys.remove(remove); //remove the first entry

            if(data.isDirty()) { //save the data we just removed, if it has been marked as dirty
                loader.save(removedValue, playerFile, id.toString());
            }
        }
    }
}