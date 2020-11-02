package io.github.zap.maploader;

import org.bukkit.World;

import java.util.function.Consumer;

public interface MapLoader {
    /**
     * Preloads the specified array of world names. The implementation should keep these cached in memory.
     * @param worldNames The worlds to preload
     */
    void preloadWorlds(String... worldNames);

    /**
     * Loads the map associated with worldName. This should copy from worlds cached via preloadWorlds, if possible.
     * Implementations of this function may run fully or partially async.
     * @param worldName The name of the world to load from
     * @param worldConsumer The consumer that is called when the world is loaded
     */
    void loadMap(String worldName, Consumer<World> worldConsumer);

    /**
     * Unloads the map associated with the name. This should remove the map from the server world list, but it should
     * not necessarily remove anything from the cache.
     * @param mapName The name of the specified map
     */
    void unloadMap(String mapName);

    /**
     * Determine if the specified world exists. It may not be loaded.
     * @param worldName The world to test
     * @return Whether or not the world exists
     */
    boolean worldExists(String worldName);
}
