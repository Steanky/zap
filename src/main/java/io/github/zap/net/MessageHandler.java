package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

/**
 * This abstraction describes a class capable of handling incoming plugin messages, usually for a specific channel.
 */
public interface MessageHandler {
    /**
     * Handles the plugin message given the invoking player and a byte array. This method is responsible for validating
     * the byte array; if it is invalid somehow an exception should NOT be thrown - simply return false.
     * @param player The originating player
     * @param message The raw message data
     * @return Whether or not the message was successfully handled.
     */
    boolean handle(Player player, byte[] message);
}
