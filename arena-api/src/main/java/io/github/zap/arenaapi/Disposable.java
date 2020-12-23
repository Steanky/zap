package io.github.zap.arenaapi;

/**
 * C#-style interface objects should implement if they acquire access to resources that will not be normally garbage-
 * collected (such as Bukkit event handlers). Implementations may throw ObjectDisposedException if an attempt is made
 * to call any functions on a disposed object.
 */
public interface Disposable {
    /**
     * Disposes this object, releasing any resources it may be holding on to.
     */
    void dispose();
}
