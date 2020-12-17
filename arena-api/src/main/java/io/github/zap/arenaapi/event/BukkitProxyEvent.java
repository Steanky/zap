package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

/**
 * Class that proxies Bukkit events to ArenaApi ones for better encapsulation and control. Event registration with
 * Bukkit occurs as-necessary — simply creating these objects will not impact performance.
 *
 * Currently only supports synchronous events.
 * @param <T> The type of Bukkit event we're wrapping
 */
public class BukkitProxyEvent<T extends Event> extends PredicatedEvent<T> implements Listener {
    private final Class<T> bukkitEventClass;
    private final EventPriority priority;
    private final Plugin plugin;
    private final boolean ignoreCancelled;

    private boolean eventRegistered = false;
    private final HandlerList handlerList;

    public BukkitProxyEvent(Plugin plugin, Predicate<T> predicate, Class<T> bukkitEventClass, EventPriority priority,
                            boolean ignoreCancelled) {
        super(predicate);

        HandlerList list;
        this.plugin = plugin;
        this.bukkitEventClass = bukkitEventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;

        try {
            list = (HandlerList)bukkitEventClass.getMethod("getHandlers").invoke(null);
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            ArenaApi.severe("Failed to construct BukkitProxyEvent due to a reflection-related exception.");
            list = null;
        }

        handlerList = list;
    }

    public BukkitProxyEvent(Plugin plugin, Predicate<T> predicate, Class<T> bukkitEventClass) {
        this(plugin, predicate, bukkitEventClass, EventPriority.NORMAL, true);
    }

    @Override
    public void registerHandler(EventHandler<T> handler) {
        super.registerHandler(handler);

        /*
        lazy registration with bukkit; lets us create as many instances of proxy events as we want without any
        performance consequences
         */
        if(handlerCount() == 1 && !eventRegistered) {
            plugin.getServer().getPluginManager().registerEvent(bukkitEventClass, this, priority,
                    (listener, event) -> callEvent(bukkitEventClass.cast(event)), plugin, ignoreCancelled);

            eventRegistered = true;
        }
    }

    @Override
    public void removeHandler(EventHandler<T> handler) {
        super.removeHandler(handler);

        if(handlerCount() == 0 && eventRegistered) {
            eventRegistered = false;
            unregister();
        }
    }

    @Override
    public void close() {
        super.close();

        if(eventRegistered) {
            eventRegistered = false;
            unregister();
        }
    }

    private void unregister() {
        if(handlerList != null) {
            handlerList.unregister(this);
        }
        else {
            ArenaApi.warning("Tried to unregister event to which we have no HandlerList reference. Using " +
                    "HandlerList#unregisterAll instead, which has performance problems.");
            HandlerList.unregisterAll(this);
        }
    }
}
