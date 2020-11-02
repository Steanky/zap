package io.github.zap.maploader;

import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import io.github.zap.ZombiesPlugin;
import io.github.zap.proxy.SlimeProxy;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class used to load a Zombies map with the Slime format
 */
public class SlimeWorldLoader implements WorldLoader {
    private final SlimeLoader slimeLoader;
    private final Map<String, SlimeWorld> preloadedWorlds = new HashMap<>();

    /**
     * Creates a new instance of SlimeMapLoader given a SlimePlugin.
     * @param slimeLoader The SlimeLoader that this instance uses
     */
    public SlimeWorldLoader(SlimeLoader slimeLoader) {
        this.slimeLoader = slimeLoader;
    }

    @SneakyThrows
    @Override
    public void preloadWorlds(String... worlds) {
        for (String world : worlds) {
            SlimeProxy slime = ZombiesPlugin.getInstance().getSlimeProxy();
            if(!slimeLoader.worldExists(world)) {
                slime.importWorld(new File(world), world, slimeLoader);
            }

            preloadedWorlds.put(world, slime.loadWorld(slimeLoader, world, true, new SlimePropertyMap()));
        }
    }

    @Override
    public void loadWorld(String name, Consumer<World> consumer) {
        String randomName = UUID.randomUUID().toString();
        SlimeWorld world = preloadedWorlds.get(name).clone(randomName);

        ZombiesPlugin.getInstance().getSlimeProxy().generate(world);
        consumer.accept(Bukkit.getWorld(randomName));
    }

    @Override
    public void unloadWorld(String mapName) {
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