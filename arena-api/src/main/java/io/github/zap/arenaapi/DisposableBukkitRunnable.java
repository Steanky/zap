package io.github.zap.arenaapi;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public abstract class DisposableBukkitRunnable extends BukkitRunnable implements Disposable {
    private boolean disposed;

    @Override
    public boolean isCancelled() throws IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return super.isCancelled();
    }

    @Override
    public void cancel() throws IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        super.cancel();
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return super.runTask(plugin);
    }

    @Override
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return super.runTaskLater(plugin, delay);
    }

    @Override
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return super.runTaskTimer(plugin, delay, period);
    }

    @Override
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        throw new NotImplementedException();
    }

    @Override
    public int getTaskId() throws IllegalStateException {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return super.getTaskId();
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        if(!isCancelled()) {
            cancel();
        }

        disposed = true;
    }
}
