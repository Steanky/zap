package io.github.zap.proxy;

import io.lumine.xikage.mythicmobs.MythicMobs;

/**
 * Access MythicMobs API through this proxy interface.
 */
public interface MythicProxy {
    /**
     * Gets the MythicMobs instance.
     * @return The underlying MythicMobs instance
     */
    MythicMobs getMythicPlugin();

    /**
     * Injects custom mechanics into MythicMobs.
     */
    void injectCustomMechanics();

    //more methods will be added here later as necessary
}
