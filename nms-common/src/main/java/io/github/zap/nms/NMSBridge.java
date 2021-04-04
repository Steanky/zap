package io.github.zap.nms;

import io.github.zap.nms.entity.EntityBridge;
import io.github.zap.nms.world.WorldBridge;
import org.jetbrains.annotations.NotNull;

/**
 * Used to provide a consistent interface between various NMS versions and ZAP-related code modules.
 */
public interface NMSBridge {
    /**
     * Returns the current NMS version as a string. This should be in the format
     * [major_version]_[minor_version]_R[package_version]; for example, 1_16_R3 for Minecraft 1.16.5 (which uses the
     * third released NMS package.
     * @return The NMS version this NMSBridge handles
     */
    @NotNull String version();

    /**
     * Returns a bridge used to proxy methods relating to NMS Entity and its subclasses.
     * @return An EntityBridge instance
     */
    @NotNull EntityBridge entityBridge();

    /**
     * Returns a bridge used to proxy methods relating to the NMS World object, as well as closely related functions.
     * @return A WorldBridge instance
     */
    @NotNull WorldBridge worldBridge();
}
