package io.github.zap.arenaapi.playerdata;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import lombok.Getter;

import java.io.File;
import java.util.*;

public class FilePlayerDataManager implements PlayerDataManager {
    @Getter
    private final File dataFolder;

    @Getter
    private final DataLoader loader;

    @Getter
    private final int memoryCacheLength;

    private final Map<UUID, FilePlayerData> cache = new LinkedHashMap<>();

    public FilePlayerDataManager(File dataFolder, DataLoader loader, int memoryCacheLength) {
        this.dataFolder = dataFolder;
        this.loader = loader;
        this.memoryCacheLength = memoryCacheLength;
    }

    @Override
    public PlayerData getPlayerData(UUID id) {
        //TODO: update to properly support new serialization API
        FilePlayerData data = cache.get(id);

        if(data != null) { //data was cached so just retrieve it
            return data;
        }

        String name = id.toString();
        try { //data was not cached; try to load the playerdata from a file
            data = loader.load(dataFolder, FilePlayerData.class);
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

        if(cache.keySet().size() > memoryCacheLength) { //if we exceeded the limit, we must remove the first entry
            FilePlayerData removedValue = cache.remove(cache.keySet().iterator().next()); //get the value so we can save it if needed

            if(data.isDirty()) { //save the data we just removed, if it has been marked as dirty
                loader.save(removedValue, dataFolder);
            }
        }
    }
}