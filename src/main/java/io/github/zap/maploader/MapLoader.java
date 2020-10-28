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
     * Loads the map associated with worldName
     * @param worldName The name of the world to load from
     * @param worldConsumer The consumer that is called when the world is loaded
     */
    void loadMap(String worldName, Consumer<World> worldConsumer);

    /**
     * Unloads the map associated with the name.
     * @param mapName The name of the specified map
     */
    void unloadMap(String mapName);
}
