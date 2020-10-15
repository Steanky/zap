package io.github.zap.net;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Singleton class responsible for passing plugin messages to their assigned handlers.
 */
public final class MessageRouter {
    @Getter
    private static final MessageRouter instance = new MessageRouter();

    private final Map<String, MessageHandler> handlers = new HashMap<>();

    private MessageRouter() { }

    /**
     * Registers a specific handler with this instance.
     * @param channel The name of the channel
     * @param handler The handler whose handle() method will be called when a message is received in
     */
    public void registerHandler(String channel, MessageHandler handler) {
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
     * Gets a collection of MessageHandler objects that are currently registered with this MessageRouter.
     * @return A List of Handler objects
     */
    public List<MessageHandler> getHandlers() {
        return new ArrayList<>(handlers.values()); //return new list so user can't modify the backing map directly
    }

    public boolean handleCustom(String channel, Player player, byte[] message) {
        MessageHandler handler = handlers.get(channel);

        if(handler != null) {
            return handler.handle(player, message);
        }

        return false;
    }
}
