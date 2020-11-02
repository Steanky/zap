package io.github.zap.proxy;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import java.io.File;

public interface SlimeProxy {
    SlimePlugin getSlimePlugin();

    SlimeLoader getLoader(String source);

    void importWorld(File worldDir, String worldName, SlimeLoader loader);

    SlimeWorld loadWorld(SlimeLoader loader, String name, boolean save, SlimePropertyMap propertyMap);

    void generate(SlimeWorld world);
}
