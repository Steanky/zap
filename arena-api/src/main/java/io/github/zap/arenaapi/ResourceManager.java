package io.github.zap.arenaapi;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Manages groups of disposable objects. In particular, ensures that all objects it manages have their dispose() method
 * called, even if one throws a RuntimeException during disposal. Uses weak references towards disposable objects to
 * avoid memory leaks.
 */
@RequiredArgsConstructor
public class ResourceManager implements Disposable {
    private final Plugin plugin;

    //weak set: its elements will be removed automatically if they are garbage collected
    private final Set<Disposable> weakDisposableSet = Collections.newSetFromMap(new WeakHashMap<>());

    private boolean disposed = false;

    /**
     * Gets the plugin that's using this ResourceManager. If this ResourceManager has been disposed, an
     * ObjectDisposedException will be thrown.
     * @return The plugin this instance belongs to
     */
    public Plugin getPlugin() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return plugin;
    }

    /**
     * Adds the given Disposable to this ResourceManager. If it already exists, an exception will be thrown and the
     * new element will not be added. If this ResourceManager has been disposed, an ObjectDisposedException will be
     * thrown.
     * @param disposable The Disposable object to add
     */
    public void addDisposable(@NotNull Disposable disposable) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        Objects.requireNonNull(disposable, "disposable cannot be null!");

        if(!weakDisposableSet.add(disposable)) {
            throw new IllegalArgumentException("you cannot add the same Disposable twice!");
        }
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        for(Disposable disposable : weakDisposableSet) {
            try {
                disposable.dispose();
            }
            catch (RuntimeException exception) {
                plugin.getLogger().warning("Disposable threw unchecked exception!\n" + exception);
            }
        }

        disposed = true;
    }
}
