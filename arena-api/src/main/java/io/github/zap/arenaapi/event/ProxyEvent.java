package io.github.zap.arenaapi.event;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Class that proxies Bukkit events to ArenaApi ones for better encapsulation and control. Event registration with
 * Bukkit occurs as-necessary — simply creating these objects will not result in any performance issues.
 * @param <T> The type of Bukkit event we're wrapping
 */
public class ProxyEvent<T extends org.bukkit.event.Event> extends Event<T> implements Listener {
    private final Class<T> bukkitEventClass;
    private final EventPriority priority;
    private final Plugin plugin;
    private final boolean ignoreCancelled;

    private boolean eventRegistered = false;
    private HandlerList handlerList;

    private RegisteredListener registeredListener;

    /**
     * Constructs a new ProxyEvent. This event wraps a Bukkit event. Instances of ProxyEvent must be properly disposed
     * of via a call to dispose().
     * @param plugin The plugin to register the Bukkit event under
     * @param bukkitEventClass The Bukkit event we're wrapping
     * @param priority The EventPriority to use for this proxy
     * @param ignoreCancelled Whether or not we ignore cancelled events. If set to true, cancelled events will not
     *                        cause this ProxyEvent to fire. If set to true, it will fire regardless.
     */
    public ProxyEvent(Plugin plugin, Class<T> bukkitEventClass, EventPriority priority,
                      boolean ignoreCancelled) {
        this.plugin = plugin;
        this.bukkitEventClass = bukkitEventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
    }

    /**
     * Constructs a new ProxyEvent with EventPriority.NORMAL and ignoring cancelled events.
     * @param plugin The plugin to register the Bukkit event under
     * @param bukkitEventClass The Bukkit event we're wrapping
     */
    public ProxyEvent(Plugin plugin, Class<T> bukkitEventClass) {
        this(plugin, bukkitEventClass, EventPriority.NORMAL, true);
    }

    @Override
    public void registerHandler(@NotNull EventHandler<T> handler) {
        super.registerHandler(handler);

        /*
        lazy registration with bukkit; lets us create as many instances of proxy events as we want without any
        performance consequences
         */
        if(handlerCount() == 1 && !eventRegistered) {
            reflectHandlerList();

            EventExecutor executor = (listener, event) -> {
                if(bukkitEventClass.isAssignableFrom(event.getClass())) {
                    if(!event.isAsynchronous()) {
                        callEvent(bukkitEventClass.cast(event));
                    }
                    else {
                        throw new IllegalStateException("ProxyEvent does not support async events!");
                    }
                }
            };

            if(handlerList != null) {
                registeredListener = new RegisteredListener(this, executor, priority, plugin, ignoreCancelled);
                handlerList.register(registeredListener);
            }
            else {
                plugin.getServer().getPluginManager().registerEvent(bukkitEventClass, this, priority, executor,
                        plugin, ignoreCancelled);
            }

            eventRegistered = true;
        }
    }

    @Override
    public void removeHandler(@NotNull EventHandler<T> handler) {
        super.removeHandler(handler);
    }

    @Override
    public void dispose() {
        if(super.disposed) {
            return;
        }

        super.dispose();

        if(eventRegistered) {
            if(handlerList != null) {
                handlerList.unregister(registeredListener);
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
