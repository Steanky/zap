package io.github.zap;

import com.grinderwolf.swm.api.SlimePlugin;
import lombok.Getter;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin {
    @Getter
    private static ZombiesPlugin instance; //singleton pattern for our root plugin class

    @Getter
    private static SlimePlugin slimePlugin;

    @Getter
    private StopWatch timer;

    @Override
    public void onLoad() { //called before onEnable()
        instance = this; //we can't give this class a constructor, so use onLoad instead for assigning singleton instance
        timer = new StopWatch();
    }

    @Override
    public void onEnable() {
        try {
            timer.start();
            super.onEnable();

            //...plugin enabling code here
            slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

            timer.stop();
            super.getLogger().log(Level.INFO, String.format("Done enabling: ~%sms", timer.getTime()));
        }
        finally { //ensure profiler gets reset even if we have an exception
            timer.reset();
        }
    }
}