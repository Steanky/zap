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
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class used to load a Zombies map with the Slime format
 */
public final class SlimeMapLoader {
    private final SlimePlugin slimePlugin;
    private final SlimeLoader slimeLoader;

    public SlimeMapLoader(SlimePlugin slimePlugin) {
        this.slimePlugin = slimePlugin;
        slimeLoader = slimePlugin.getLoader("file");
    }

    /**
     * Duplicates and loads a Zombies map world
     * @param name The name of the map to load
     * @param consumer Consumer to execute when the map has loaded
     */
    public void loadMap(String name, Consumer<World> consumer) {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskAsynchronously(zombiesPlugin, () -> {
            try {
                SlimeWorld world = slimePlugin.loadWorld(slimeLoader, name, true, new SlimePropertyMap()).clone(UUID.randomUUID().toString());

                scheduler.runTask(zombiesPlugin, () -> {
                    slimePlugin.generateWorld(world);
                    consumer.accept(Bukkit.getWorld(name));
                });
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldInUseException e) {
                e.printStackTrace();
                consumer.accept(null);
            }
        });
    }
}
