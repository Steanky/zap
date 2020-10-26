package io.github.zap.swm;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import com.grinderwolf.swm.plugin.loaders.file.FileLoader;
import io.github.zap.ZombiesPlugin;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class used to load a Zombies map with the Slime format
 */
public class SlimeMapLoader {
    private final SlimePlugin slimePlugin;
    private final SlimeLoader slimeLoader;
    private final Map<String, SlimeWorld> preloadedWorlds = new HashMap<>();

    /**
     * Creates a new instance of SlimeMapLoader given a SlimePlugin.
     * @param slimePlugin The SlimePlugin that this instance uses
     */
    public SlimeMapLoader(SlimePlugin slimePlugin) {
        this.slimePlugin = slimePlugin;
        slimeLoader = new FileLoader(new File(String.format("plugins/%s/maps", ZombiesPlugin.getInstance().getName())));
    }

    /**
     * Preloads a series of named worlds and adds them to an internal map.
     * @param worlds The worlds to preload
     */
    @SneakyThrows
    public void preloadWorlds(String... worlds) {
        for (String world : worlds) {
            if(!slimeLoader.worldExists(world)) {
                slimePlugin.importWorld(new File(world), world, slimeLoader);
            }

            preloadedWorlds.put(world, slimePlugin.loadWorld(slimeLoader, world, true, new SlimePropertyMap()));
        }
    }

    /**
     * Loads a Zombies map world from memory
     * @param name The name of the map to load
     */
    public void loadMap(String name, Consumer<World> consumer) {
        String randomName = UUID.randomUUID().toString();
        SlimeWorld world = preloadedWorlds.get(name).clone(randomName);

        slimePlugin.generateWorld(world);
        consumer.accept(Bukkit.getWorld(randomName));
    }
}