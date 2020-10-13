package io.github.zap.swm;

/**
 * Callback class to load slime Zombies maps
 */
public interface SlimeMapLoadCallback {

    /**
     * Run when a slime Zombies map loads.
     * @param name The loaded world's name. Returns null if the world failed to load.
     */
    void onSlimeMapLoad(String name);

}
