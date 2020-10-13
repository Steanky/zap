package io.github.zap;

import com.google.common.io.ByteStreams;
import io.github.zap.game.ArenaManager;
import io.github.zap.manager.PlayerRouter;
import io.github.zap.net.BungeeHandler;
import io.github.zap.net.MessageRouter;
import lombok.Getter;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class ZombiesPlugin extends JavaPlugin implements PluginMessageListener {
    @Getter
    private static ZombiesPlugin instance; //singleton pattern for our main plugin class

    /*
    Warning! This object is NOT thread safe! Only call if you're on the main server thread. Also make sure you always
    call timer.reset() after a call to timer.start() (use a finally block in case of exceptions!)
     */
    @Getter
    private StopWatch timer;

    /*
    if this plugin is marked as being the root instance, PlayerRouter will be capable of sending players to different
    linked bungeecord servers, which will in turn just call their own instance of PlayerRouter to add them to games. if
    it's not (or bungeecord isn't implemented), PlayerRouter will just be a regular instance of ArenaManager.
     */
    @Getter
    private PlayerRouter router;

    /*
    Responsible for handling all incoming plugin messages.
     */
    @Getter
    private MessageRouter messageRouter;

    @Override
    public void onEnable() {
        instance = this;
        timer = new StopWatch();

        try {
            timer.start();
            super.onEnable();
            //...plugin enabling code below

            initConfig();

            if(getConfig().getBoolean(ConfigVariables.ROOT_INSTANCE)) {
                //set router to an implementation of PlayerRouter that is capable of sending players to other servers
            }
            else {
                //set router to a regular ArenaManager
                router = new ArenaManager();
            }

            initMessaging();

            timer.stop();
            super.getLogger().log(Level.INFO, String.format("Done enabling: ~%sms", timer.getTime()));
        }
        finally { //ensure profiler gets reset even if we have an exception
            timer.reset();
        }
    }

    @Override
    public void onDisable() {
        //perform shutdown tasks
    }

    private void initConfig() {
        FileConfiguration config = this.getConfig();

        /*
        disable root_instance by default; enable only for the plugin instance running on the server people can directly
        join, because it must run a special implementation of PlayerRouter that can transmit players across BungeeCord
         */
        config.addDefault(ConfigVariables.ROOT_INSTANCE, false);
        config.options().copyDefaults(true);
        this.saveConfig();
    }

    private void initMessaging() {
        MessageRouter.getInstance().registerHandler(ChannelNames.BUNGEECORD, new BungeeHandler());

        Messenger messenger = this.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, ChannelNames.BUNGEECORD);
        messenger.registerIncomingPluginChannel(this, ChannelNames.BUNGEECORD, this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        messageRouter.handle(channel, player, ByteStreams.newDataInput(message));
    }
}