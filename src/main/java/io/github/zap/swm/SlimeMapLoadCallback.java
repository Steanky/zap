package io.github.zap.swm;

import org.bukkit.World;

/**
 * Callback class to load slime Zombies maps
 */
public interface SlimeMapLoadCallback {

    /**
     * Run when a slime Zombies map loads.
     * @param world The loaded world. Returns null if the world failed to load.
     */
    void onSlimeMapLoad(World world);

}
