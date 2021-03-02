package io.github.zap.arenaapi.playerdata;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import lombok.Getter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class FilePlayerDataManager implements PlayerDataManager {
    @Getter
    private final DataLoader loader;

    @Getter
    private final int memoryCacheLength;

    private final Map<UUID, FilePlayerData> cache = new LinkedHashMap<>();

    public FilePlayerDataManager(DataLoader loader, int memoryCacheLength) {
        this.loader = loader;
        this.memoryCacheLength = memoryCacheLength;
    }

    @Override
    public PlayerData getPlayerData(UUID id) {
        FilePlayerData data = cache.get(id);

        if(data != null) { //data was cached so just retrieve it
            return data;
        }

        File dataFile = loader.getFile(id.toString());

        if(!dataFile.exists()) { //create playerdata file if missing
            FilePlayerData newData = new FilePlayerData();
            cacheMapping(id, newData);
            saveData(newData, id);
            return newData;
        }
        else {
            data = loader.load(id.toString(), FilePlayerData.class);

            if(data != null) { //loaded data from file, cache it in case of further use
                cacheMapping(id, data);
                return data;
            }
            else {
                ArenaApi.warning(String.format("Unable to load playerdata from file %s.", dataFile.getPath()));
                return null;
            }
        }
    }

    @Override
    public void flushAll() {
        for(Map.Entry<UUID,FilePlayerData> entry : cache.entrySet()) {
            FilePlayerData value = entry.getValue();

            if(value.isDirty()) {
                saveData(value, entry.getKey());
            }
        }
    }

    private void cacheMapping(UUID id, FilePlayerData data) {
        cache.put(id, data); //add the mapping to the cache

        if(cache.keySet().size() > memoryCacheLength) { //if we exceeded the limit, we must remove the first entry
            FilePlayerData removedValue = cache.remove(cache.keySet().iterator().next()); //get the value so we can save it if needed

            if(data.isDirty()) { //save the data we just removed, if it has been marked as dirty
                saveData(removedValue, id);
            }
        }
    }

    private void saveData(FilePlayerData data, UUID id) {
        loader.save(data, id.toString());
    }
}
