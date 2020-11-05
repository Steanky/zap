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

    @Override
    public void preloadWorlds(String... worlds) {
        try {
            for (String world : worlds) {
                preloadedWorlds.put(world, readWorld(world));
            }
        }
        catch(IOException e) {
            ZombiesPlugin.getInstance().getLogger().severe(String.format("IOException when attempting to preload " +
                    "worlds: %s", e.getMessage()));
        }
    }

    @Override
    public void loadWorld(String worldName, Consumer<World> onLoad) {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        SlimeWorld base = preloadedWorlds.get(worldName);

        if(base == null) {
            zombiesPlugin.getLogger().warning(String.format("Trying to load world '%s' in loadWorld function, as" +
                    " it was not preloaded.", worldName));
            try {
                base = readWorld(worldName);
            }
            catch(IOException e) {
                zombiesPlugin.getLogger().severe(String.format("IOException when attempting to load world '%s': %s",
                        worldName, e.getMessage()));
                return;
            }
        }

        String randomName = UUID.randomUUID().toString();
        SlimeWorld world = base.clone(randomName);
        zombiesPlugin.getSlimeProxy().generate(world);

        World generatedWorld = Bukkit.getWorld(randomName);
        if(generatedWorld != null) {
            onLoad.accept(Bukkit.getWorld(randomName));
        }
        else {
            zombiesPlugin.getLogger().severe(String.format("World '%s' was just generated, but it could not be found" +
                    "on the Bukkit world list.", randomName));
        }
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
            ZombiesPlugin.getInstance().getLogger().severe(String.format("Exception when trying to determine if" +
                    "world '%s' exists: %s", worldName, e.getMessage()));
        }

        return false;
    }

    private SlimeWorld readWorld(String worldName) throws IOException {
        SlimeProxy slime = ZombiesPlugin.getInstance().getSlimeProxy();
        if(!slimeLoader.worldExists(worldName)) {
            slime.importWorld(new File(worldName), worldName, slimeLoader);
        }

        return slime.loadWorld(slimeLoader, worldName, true, new SlimePropertyMap());
    }
}