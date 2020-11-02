package io.github.zap.proxy;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;

@RequiredArgsConstructor
public class SlimeWorldManager_v2_3_R0 implements SlimeProxy {
    private final SlimePlugin slimePlugin;

    @Override
    public SlimeLoader getLoader(String source) {
        return slimePlugin.getLoader(source);
    }

    @Override
    @SneakyThrows
    public void importWorld(File worldDir, String worldName, SlimeLoader loader) {
        slimePlugin.importWorld(worldDir, worldName, loader);
    }

    @Override
    @SneakyThrows
    public SlimeWorld loadWorld(SlimeLoader loader, String name, boolean save, SlimePropertyMap propertyMap) {
        return slimePlugin.loadWorld(loader, name, save, propertyMap);
    }

    @Override
    public void generate(SlimeWorld world) {
        slimePlugin.generateWorld(world);
    }
}
