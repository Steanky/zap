package io.github.zap.arenaapi.world;

import org.bukkit.World;

import java.util.function.Consumer;

public interface WorldLoader {
    /**
     * Preloads all necessary worlds for this implementation.
     */
    void preload();

    /**
     * Loads the map associated with worldName. This should copy from worlds cached via preload, if possible.
     * Implementations of this function may run fully or partially async.
     * @param worldName The name of the world to load from
     * @param worldConsumer The consumer that is called when the world is loaded. This should always be run by the main
     *                      server thread
     */
    void loadWorld(String worldName, Consumer<World> worldConsumer);

    /**
     * Unloads the specified world. Implementations may throw an exception if the world is unloaded already.
     * @param world The name of the specified world to unload
     */
    void unloadWorld(World world);

    /**
     * Determine if a world with the specified name exists.
     * @param worldName The world name to test
     * @return Whether or not a world with the provided name exists
     */
    boolean worldExists(String worldName);
}