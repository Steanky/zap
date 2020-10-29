package io.github.zap.maploader;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import io.github.zap.ZombiesPlugin;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Class used to load a Zombies map with the Slime format
 */
public class SlimeMapLoader implements MapLoader {
    private final SlimePlugin slimePlugin;
    private final SlimeLoader slimeLoader;
    private final Map<String, SlimeWorld> preloadedWorlds = new HashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Creates a new instance of SlimeMapLoader given a SlimePlugin.
     * @param slimePlugin The SlimePlugin that this instance uses
     * @param slimeLoader The SlimeLoader that this instance uses
     */
    public SlimeMapLoader(SlimePlugin slimePlugin, SlimeLoader slimeLoader) {
        this.slimePlugin = slimePlugin;
        this.slimeLoader = slimeLoader;
    }

    @SneakyThrows
    @Override
    public void preloadWorlds(String... worlds) {
        for (String world : worlds) {
            if(!slimeLoader.worldExists(world)) {
                slimePlugin.importWorld(new File(world), world, slimeLoader);
            }

            preloadedWorlds.put(world, slimePlugin.loadWorld(slimeLoader, world, true, new SlimePropertyMap()));
        }
    }

    @Override
    public void loadMap(String name, Consumer<World> consumer) {
        String randomName = UUID.randomUUID().toString();
        SlimeWorld world = preloadedWorlds.get(name).clone(randomName);

        slimePlugin.generateWorld(world);
        consumer.accept(Bukkit.getWorld(randomName));
    }

    @Override
    public void unloadMap(String mapName) {
        Bukkit.unloadWorld(mapName, false);
    }

    @Override
    public boolean worldExists(String worldName) {
        try {
            return slimeLoader.worldExists(worldName);
        }
        catch(IOException e) {
            ZombiesPlugin.getInstance().getLogger().warning(String.format("Exception when trying to determine if" +
                    "world '%s' exists: %s", worldName, e.getMessage()));
        }

        return false;
    }
}