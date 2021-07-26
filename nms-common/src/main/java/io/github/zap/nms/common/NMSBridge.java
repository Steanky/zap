package io.github.zap.nms.common;

import io.github.zap.nms.common.entity.EntityBridge;
import io.github.zap.nms.common.itemstack.ItemStackBridge;
import io.github.zap.nms.common.player.PlayerBridge;
import io.github.zap.nms.common.world.WorldBridge;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Used to provide a consistent interface between various NMS versions and ZAP-related code modules.
 */
public interface NMSBridge {
    String CURRENT_NMS_VERSION = nmsVersion();

    private static String nmsVersion() {
        String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitVersion.substring(bukkitVersion.lastIndexOf('.') + 1);
    }

    /**
     * Returns this bridge's NMS version as a string. This should be in the format
     * v[major_version]_[minor_version]_R[package_version]; for example, v1_16_R3 for Minecraft 1.16.5 (which uses the
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
     * Returns a bridge used to proxy methods relating to item stacks.
     * @return A PlayerBridge instance
     */
    @NotNull ItemStackBridge itemStackBridge();

    /**
     * Returns a bridge used to proxy methods relating to players.
     * @return A PlayerBridge instance
     */
    @NotNull PlayerBridge playerBridge();

    /**
     * Returns a bridge used to proxy methods relating to the NMS World object, as well as closely related functions.
     * @return A WorldBridge instance
     */
    @NotNull WorldBridge worldBridge();

    /**
     * Tries to select the NMSBridge instance for the current NMS version, using the provided bridges. Each bridges'
     * reported version strings are used for comparison. Versions comparison is case-sensitive; that is, v1_16_R3 is
     * different from V1_16_R3. If multiple bridges are compatible, the first will be selected.
     * @param bridges The bridges to select from.
     * @return The first compatible NMSBridge instance, or null if none can be found
     */
    static @Nullable NMSBridge selectBridge(@NotNull NMSBridge ... bridges) {
        Objects.requireNonNull(bridges, "bridges cannot be null!");

        for(NMSBridge bridge : bridges) {
            if(bridge.version().equals(CURRENT_NMS_VERSION)) {
                return bridge;
            }
        }

        return null;
    }
}
