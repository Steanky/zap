package io.github.zap.proxy;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import java.io.File;
import java.io.IOException;

public interface SlimeProxy {
    File getSlimeWorldDirectory();

    String getSlimeFileExtension();

    SlimePlugin getSlimePlugin();

    SlimeLoader getLoader(String source);

    void importWorld(File worldDir, String worldName, SlimeLoader loader) throws WorldTooBigException, InvalidWorldException, IOException, WorldAlreadyExistsException, WorldLoadedException;

    SlimeWorld loadWorld(SlimeLoader loader, String name, boolean save, SlimePropertyMap propertyMap) throws NewerFormatException, CorruptedWorldException, WorldInUseException, UnknownWorldException, IOException;

    void generate(SlimeWorld world);
}
