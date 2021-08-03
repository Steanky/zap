package io.github.zap.arenaapi.nms.common.player;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A bridge for methods relating to players
 */
public interface PlayerBridge {

    /**
     * Gets a wrapped signed property of a player's skin texture
     * @param player The player to get the skin from
     * @return A wrapped sign property of the texture
     */
    @Nullable WrappedSignedProperty getSkin(@NotNull Player player);

}
