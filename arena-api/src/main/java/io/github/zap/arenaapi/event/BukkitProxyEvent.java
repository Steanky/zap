package io.github.zap.arenaapi.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.function.Predicate;

/**
 * Class that proxies Bukkit events to ArenaApi ones for better encapsulation and control. Event registration with
 * Bukkit occurs as-necessary — simply creating these objects will not impact performance.
 *
 * Currently only supports synchronous events.
 * @param <T> The type of Bukkit event we're wrapping
 */
public class BukkitProxyEvent<T extends org.bukkit.event.Event> extends PredicatedEvent<T> implements Listener {
    private final Class<T> bukkitEventClass;
    private final EventPriority priority;
    private final Plugin plugin;
    private final boolean ignoreCancelled;

    private boolean eventRegistered = false;

    public BukkitProxyEvent(Plugin plugin, Predicate<T> predicate, Class<T> bukkitEventClass, EventPriority priority,
                            boolean ignoreCancelled) {
        super(predicate);

        this.plugin = plugin;
        this.bukkitEventClass = bukkitEventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
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
            HandlerList.unregisterAll(this);
            eventRegistered = false;
        }
    }

    @Override
    public void close() {
        super.close();

        if(eventRegistered) {
            HandlerList.unregisterAll(this);
            eventRegistered = false;
        }
    }
}
