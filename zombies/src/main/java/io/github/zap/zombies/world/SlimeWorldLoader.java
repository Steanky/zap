package io.github.zap.zombies.world;

import com.google.common.io.Files;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.SWMPlugin;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.Zombies;
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void preload() {
        Zombies zombies = Zombies.getInstance();
        SWMPlugin slime = zombies.getSWM();
        File[] files = zombies.getSlimeWorldDirectory().listFiles();

        if(files != null) {
            for(File worldFile : files) {
                String worldFileName = worldFile.getName();

                if(worldFileName.endsWith(zombies.getSlimeExtension())) {
                    String worldName = Files.getNameWithoutExtension(worldFileName);

                    try {
                        preloadedWorlds.put(worldName, slime.loadWorld(slimeLoader, worldName, true,
                                new SlimePropertyMap()));
                    }
                    catch(IOException | CorruptedWorldException | WorldInUseException | NewerFormatException |
                            UnknownWorldException e) {
                        Zombies.getInstance().getLogger().severe(String.format("Exception when attempting to preload " +
                                "world '%s': %s", worldName, e.getMessage()));
                    }
                }
                else {
                    Zombies.getInstance().getLogger().warning(String.format("Ignoring non-SWF file '%s'",
                            worldFileName));
                }
            }
        }
    }

    @Override
    public void loadWorld(String worldName, Consumer<World> onLoad) {
        Zombies zombies = Zombies.getInstance();
        SlimeWorld base = preloadedWorlds.get(worldName);

        if(base != null) {
            String randomName = UUID.randomUUID().toString();
            SlimeWorld world = base.clone(randomName);
            zombies.getSWM().generateWorld(world);

            World generatedWorld = Bukkit.getWorld(randomName);
            if(generatedWorld != null) {
                onLoad.accept(Bukkit.getWorld(randomName));
            }
            else {
                zombies.getLogger().severe(String.format("World '%s' was just generated, but it could not be " +
                        "found on the Bukkit world list.", randomName));
            }
        }
        else {
            zombies.getLogger().severe(String.format("Requested world '%s' could not be found.", worldName));
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
            Zombies.getInstance().getLogger().warning(String.format("Exception when trying to determine if world '%s' " +
                    "exists: %s", worldName, e.getMessage()));
        }

        return false;
    }
}