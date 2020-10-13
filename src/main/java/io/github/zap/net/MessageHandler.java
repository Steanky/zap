package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

import java.util.*;

public class MessageHandler {
    public interface Handler {
        void handle(Player player, ByteArrayDataInput data);
    }

    private final Map<String, Handler> handlers = new HashMap<>();

    /**
     * Registers a specific handler with this instance.
     * @param channel The name of the channel
     * @param handler The handler whose handle() method will be called when a message is received in
     */
    public void registerHandler(String channel, Handler handler) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(handler, "handler cannot be null");

        if(!handlers.containsKey(channel)) {
            handlers.put(channel, handler);
        }
        else {
            throw new IllegalArgumentException("a handler for that channel already exists");
        }
    }

    /**
     * Gets a collection of MessageHandler objects that are currently registered with this MessageHandler.
     * @return A List of Handler objects
     */
    public List<Handler> getHandlers() {
        return new ArrayList<>(handlers.values()); //return new list so user can't modify the backing map directly
    }

    /**
     * Calls the associated handler for the given ByteArrayDataInput, assuming standard BungeeCord protocol
     *
     * @param player The associated player
     * @param input The input stream/byte array we're dealing with
     * @return true if the message was handled, false otherwise
     */
    public boolean handle(String channel, Player player, ByteArrayDataInput input) {
        Handler handler = handlers.get(channel);

        if(handler != null) {
            handler.handle(player, input);
            return true;
        }

        return false;
    }
}
