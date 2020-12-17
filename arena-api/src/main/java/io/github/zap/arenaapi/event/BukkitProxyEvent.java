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
            /*
            TODO: find a faster way to unregister, as even a janky reflection hack would probably be significantly
             faster than what this cursed function does. seriously, this piece of work should come with a warning that
             calling it will result in a massive performance hit. my sanity is slowly declining and it's all because of
             you, Bukkit. why did you have to do this to me? don't you understand? it's not OKAY to iterate through
             every. single. listener that has ever been registered by every single plugin, every single time you want
             to remove a specific listener. USE A HASHMAP FOR THE LOVE OF NOTCH. oh, and it also enters a monitor on
             the arraylist used to store handlers. so it's probably going to block not ONLY the main thread, but ALSO
             potentially some async tasks too. great job! :D :D :D
             */

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
