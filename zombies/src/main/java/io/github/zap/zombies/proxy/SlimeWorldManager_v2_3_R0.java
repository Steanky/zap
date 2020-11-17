package io.github.zap.zombies.proxy;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class SlimeWorldManager_v2_3_R0 implements SlimeProxy {
    private static final File SLIME_WORLD_DIR = new File("slime_worlds");
    private static final String SLIME_FILE_EXTENSION = ".slime";
    private final SlimePlugin slimePlugin;

    @Override
    public File getSlimeWorldDirectory() {
        return SLIME_WORLD_DIR;
    }

    @Override
    public String getSlimeFileExtension() {
        return SLIME_FILE_EXTENSION;
    }

    @Override
    public SlimePlugin getSlimePlugin() { return slimePlugin; }

    @Override
    public SlimeLoader getLoader(String source) {
        return slimePlugin.getLoader(source);
    }

    @Override
    public void importWorld(File worldDir, String worldName, SlimeLoader loader) throws WorldTooBigException,
            InvalidWorldException, IOException, WorldAlreadyExistsException, WorldLoadedException {
        slimePlugin.importWorld(worldDir, worldName, loader);
    }

    @Override
    public SlimeWorld loadWorld(SlimeLoader loader, String name, boolean save, SlimePropertyMap propertyMap)
            throws NewerFormatException, CorruptedWorldException, WorldInUseException, UnknownWorldException,
            IOException {
        return slimePlugin.loadWorld(loader, name, save, propertyMap);
    }

    @Override
    public void generate(SlimeWorld world) {
        slimePlugin.generateWorld(world);
    }
}
