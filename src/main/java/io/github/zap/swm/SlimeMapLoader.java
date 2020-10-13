package io.github.zap.swm;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import io.github.zap.ZombiesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.util.UUID;

/**
 * Class used to duplicate or unload a Zombies map with the Slime format
 */
public final class SlimeMapLoader {

    /**
     * Duplicates and loads a Zombies map world
     * @param name The name of the map to load
     * @param callback Callback to execute when the map has loaded
     */
    public static void duplicateMap(String name, SlimeMapLoadCallback callback) {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        SlimePlugin slimePlugin = ZombiesPlugin.getSlimePlugin();
        SlimeLoader loader = slimePlugin.getLoader("file");

        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskAsynchronously(zombiesPlugin, () -> {
            try {
                SlimeWorld world = slimePlugin.loadWorld(loader, name, true, new SlimePropertyMap()).clone(UUID.randomUUID().toString());

                scheduler.runTask(zombiesPlugin, () -> {
                    slimePlugin.generateWorld(world);
                    callback.onSlimeMapLoad(name);
                });
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldInUseException e) {
                e.printStackTrace();
                callback.onSlimeMapLoad(null);
            }
        });
    }

}
