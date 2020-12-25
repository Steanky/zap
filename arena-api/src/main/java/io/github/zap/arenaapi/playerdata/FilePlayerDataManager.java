package io.github.zap.arenaapi.playerdata;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.DataLoader;
import lombok.Getter;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.File;
import java.nio.file.Path;
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

        //noinspection ResultOfMethodCallIgnored
        dataFolder.mkdirs();
    }

    @Override
    public PlayerData getPlayerData(UUID id) {
        FilePlayerData data = cache.get(id);

        if(data != null) { //data was cached so just retrieve it
            return data;
        }

        String name = id.toString() + '.' + loader.getExtension();
        File dataFile = Path.of(dataFolder.getPath(), name).toFile();

        if(!dataFile.exists()) { //create playerdata file if missing
            FilePlayerData newData = new FilePlayerData();
            cacheMapping(id, newData);
            loader.save(newData, dataFile);
            return newData;
        }
        else {
            data = loader.load(dataFile, FilePlayerData.class);

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
                loader.save(value, Path.of(dataFolder.getPath(), entry.getKey().toString() + '.' +
                        loader.getExtension()).toFile());
            }
        }
    }

    private void cacheMapping(UUID id, FilePlayerData data) {
        cache.put(id, data); //add the mapping to the cache

        if(cache.keySet().size() > memoryCacheLength) { //if we exceeded the limit, we must remove the first entry
            FilePlayerData removedValue = cache.remove(cache.keySet().iterator().next()); //get the value so we can save it if needed

            if(data.isDirty()) { //save the data we just removed, if it has been marked as dirty
                loader.save(removedValue, dataFolder);
            }
        }
    }
}