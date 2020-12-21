package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.Unique;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

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
    private boolean reflectionFailed = false;

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
            plugin.getServer().getPluginManager().registerEvent(bukkitEventClass, this, priority,
                    (listener, event) -> callEvent(bukkitEventClass.cast(event)), plugin, ignoreCancelled);

            eventRegistered = true;
            addProxy(handlingInstance, this);
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

    /**
     * Closes all ProxyEvent instances associated with the provided Unique, and removes the Unique's mapping from
     * the internal map.
     * @param instance The instance to clear values for
     */
    public static void closeAll(Unique instance) {
        UUID id = instance.getId();
        List<ProxyEvent<?>> proxyEvents = proxies.get(id);

        if(proxyEvents != null) {
            for(ProxyEvent<?> event : proxyEvents) {
                event.close();
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
        if(handlerList == null && !reflectionFailed) {
            getHandlerList();
        }

        if(handlerList != null) {
            handlerList.unregister(this);
        }
        else {
            ArenaApi.warning("Using slow method of handler un-registration due to a reflection-related exception.");
            HandlerList.unregisterAll(this);
        }

        removeProxy(handlingInstance, this);
    }

    private void getHandlerList() {
        HandlerList list;

        try {
            list = (HandlerList)bukkitEventClass.getMethod("getHandlers").invoke(null);
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            ArenaApi.warning("Failed to construct ProxyEvent due to a reflection-related exception.");
            list = null;
            reflectionFailed = true;
        }

        handlerList = list;
    }
}