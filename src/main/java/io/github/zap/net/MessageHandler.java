package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

public interface MessageHandler {
    /**
     * Handles the plugin message given the invoking player and a ByteArrayInputData wrapping a byte array.
     * @param player The originating player
     * @param data The data
     */
    void handle(Player player, ByteArrayDataInput data);
}
