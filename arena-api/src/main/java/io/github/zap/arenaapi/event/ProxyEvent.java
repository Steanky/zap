package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Unique;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Class that proxies Bukkit events to ArenaApi ones for better encapsulation and control. Event registration with
 * Bukkit occurs as-necessary — simply creating these objects will not result in any performance issues.
 * @param <T> The type of Bukkit event we're wrapping
 */
public class ProxyEvent<T extends org.bukkit.event.Event> extends Event<T> implements Listener {
    private final Unique handlingInstance;
    private final Class<T> bukkitEventClass;
    private final EventPriority priority;
    private final Plugin plugin;
    private final boolean ignoreCancelled;

    private boolean eventRegistered = false;
    private HandlerList handlerList;

    private static final Map<UUID, List<ProxyEvent<?>>> proxies = new HashMap<>();

    public ProxyEvent(Plugin plugin, Unique handlingInstance, Class<T> bukkitEventClass, EventPriority priority,
                      boolean ignoreCancelled) {
        this.handlingInstance = handlingInstance;
        this.plugin = plugin;
        this.bukkitEventClass = bukkitEventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
    }

    public ProxyEvent(Plugin plugin, Unique handlingInstance, Class<T> bukkitEventClass) {
        this(plugin, handlingInstance, bukkitEventClass, EventPriority.NORMAL, true);
    }

    @Override
    public void registerHandler(EventHandler<T> handler) {
        super.registerHandler(handler);

        /*
        lazy registration with bukkit; lets us create as many instances of proxy events as we want without any
        performance consequences
         */
        if(handlerCount() == 1 && !eventRegistered) {
            reflectHandlerList();

            EventExecutor executor = (listener, event) -> callEvent(bukkitEventClass.cast(event));

            if(handlerList != null) {
                handlerList.register(new RegisteredListener(this, executor, priority, plugin, ignoreCancelled));
            }
            else {
                plugin.getServer().getPluginManager().registerEvent(bukkitEventClass, this, priority, executor,
                        plugin, ignoreCancelled);
            }

            eventRegistered = true;
            addProxy(handlingInstance, this);
        }
    }

    @Override
    public void removeHandler(EventHandler<T> handler) {
        super.removeHandler(handler);

        if(handlerCount() == 0 && eventRegistered) {
            unregister();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if(eventRegistered) {
            unregister();
        }
    }

    /**
     * Closes all ProxyEvent instances associated with the provided Unique, and removes the Unique's mapping from
     * the internal map.
     * @param instance The instance to clear values for
     */
    public static void closeAll(Unique instance) {
        UUID id = instance.getId();
        List<ProxyEvent<?>> proxyEvents = proxies.get(id);

        if(proxyEvents != null) {
            for(int i = proxyEvents.size() - 1; i > -1; i--) {
                proxyEvents.get(i).dispose();
            }

            proxies.remove(id);
        }
    }

    private static void addProxy(Unique instance, ProxyEvent<?> event) {
        UUID id = instance.getId();
        List<ProxyEvent<?>> list = proxies.computeIfAbsent(id, ignored -> new ArrayList<>());
        list.add(event);
    }

    private static void removeProxy(Unique instance, ProxyEvent<?> event) {
        UUID id = instance.getId();
        List<ProxyEvent<?>> list = proxies.get(id);

        if(list != null) {
            list.remove(event);

            if(list.size() == 0) {
                proxies.remove(id);
            }
        }
    }

    private void unregister() {
        if(eventRegistered) {
            if(handlerList != null) {
                eventRegistered = false;
                handlerList.unregister(this);
                removeProxy(handlingInstance, this);
            }
            else {
                ArenaApi.warning("Had to use slow method of handler unregistration; handlerList was null.");
                HandlerList.unregisterAll(this);
            }
        }
    }

    private void reflectHandlerList() {
        if(handlerList == null) {
            try {
                handlerList = (HandlerList)bukkitEventClass.getMethod("getHandlerList").invoke(null);
            }
            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException ignored) {
                ArenaApi.warning("Failed to reflect getHandlerList due to a reflection-related exception.");
                ArenaApi.warning("Name of event class we couldn't reflect: " + bukkitEventClass.getName());
                ArenaApi.warning("This shouldn't cause bugs or crashes, but may reduce performance.");
            }
        }
    }
}