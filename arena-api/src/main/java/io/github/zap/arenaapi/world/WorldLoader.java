package io.github.zap.arenaapi.world;

import org.bukkit.World;

import java.util.function.Consumer;

public interface WorldLoader {
    /**
     * Preloads all necessary worlds for this implementation.
     */
    void preload();

    /**
     * Loads the map associated with worldName. This should copy from worlds cached via preloadWorlds, if possible.
     * Implementations of this function may run fully or partially async.
     * @param worldName The name of the world to load from
     * @param worldConsumer The consumer that is called when the world is loaded. This should always be run on the main
     *                      server thread
     */
    void loadWorld(String worldName, Consumer<World> worldConsumer);

    /**
     * Unloads the map associated with the name. This must remove the map from the server world list, but it is not
     * required to remove anything from the world cache.
     * @param mapName The name of the specified map
     */
    void unloadWorld(String mapName);

    /**
     * Determine if the specified world exists. It may not be loaded.
     * @param worldName The world to test
     * @return Whether or not the world exists
     */
    boolean worldExists(String worldName);
}