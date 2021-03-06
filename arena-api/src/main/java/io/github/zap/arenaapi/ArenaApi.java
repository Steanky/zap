package io.github.zap.arenaapi;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.zap.arenaapi.game.arena.ArenaPlayer;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.ConditionStage;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.proxy.NMSProxy;
import io.github.zap.arenaapi.proxy.NMSProxy_v1_16_R3;
import io.github.zap.arenaapi.serialize.BoundingBoxDeserializer;
import io.github.zap.arenaapi.serialize.BoundingBoxSerializer;
import io.github.zap.arenaapi.serialize.VectorDeserializer;
import io.github.zap.arenaapi.serialize.VectorSerializer;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class ArenaApi extends JavaPlugin implements Listener {
    @Getter
    private static ArenaApi instance;

    @Getter
    private NMSProxy nmsProxy;

    @Getter
    private ProtocolLib protocolLib;

    @Getter
    private SimpleModule module;

    @Getter
    private ObjectMapper mapper;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    private final Map<UUID, ArenaPlayer> players = new HashMap<>();

    private static final ConditionStage lobby = new ConditionStage(player -> {
        player.setInvulnerable(true);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setFallDistance(0);
    }, player -> {

    }, false);

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();
        instance = this;

        try {
            initProxy();
            initDependencies();
            initMapper();
            Bukkit.getPluginManager().registerEvents(this, this);
        }
        catch(LoadFailureException exception)
        {
            severe(String.format("A fatal error occurred that prevented the plugin from enabling properly: '%s'.",
                    exception.getMessage()));
            getPluginLoader().disablePlugin(this, true);
            return;
        }

        timer.stop();
        getLogger().info(String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }


    private void initProxy() throws LoadFailureException {
        switch (Bukkit.getBukkitVersion()) {
            case "1.16.4-R0.1-SNAPSHOT":
            case "1.16.5-R0.1-SNAPSHOT":
                nmsProxy = new NMSProxy_v1_16_R3();
                break;
            default:
                throw new LoadFailureException(String.format("Unsupported MC version '%s'.", Bukkit.getBukkitVersion()));
        }
    }

    private void initDependencies() throws LoadFailureException {
        protocolLib = getRequiredPlugin(PluginNames.PROTOCOL_LIB, true);
    }

    private void initMapper() {
        module = new SimpleModule();

        module.addSerializer(org.bukkit.util.Vector.class, new VectorSerializer());
        module.addDeserializer(Vector.class, new VectorDeserializer());

        module.addSerializer(BoundingBox.class, new BoundingBoxSerializer());
        module.addDeserializer(BoundingBox.class, new BoundingBoxDeserializer());

        mapper = new ObjectMapper();
        mapper.registerModule(module);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public void registerArenaManager(ArenaManager<?> manager) {
        Validate.notNull(manager, "manager cannot be null");
        Validate.isTrue(arenaManagers.putIfAbsent(manager.getGameName(), manager) == null,
                "a manager for game type '%s' has already been registered");
    }

    public ArenaManager<?> getArenaManager(String name) {
        return arenaManagers.get(name);
    }

    public Map<String, ArenaManager<?>> getArenaMangers() {
        return Collections.unmodifiableMap(arenaManagers);
    }

    public void handleJoin(JoinInformation information, Consumer<Pair<Boolean, String>> onCompletion) {
        String gameName = information.getGameName();
        ArenaManager<?> arenaManager = arenaManagers.get(gameName);

        if(arenaManager != null) {
            arenaManager.handleJoin(information, onCompletion);
        }
        else {
            warning(String.format("Invalid JoinInformation received: '%s' is not a game.", gameName));
        }
    }

    public ArenaPlayer getArenaPlayer(UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Adds a deserializer to the module
     * @param type The type of the class to deserialize
     * @param deserializer The deserializer itself
     * @param <T> The type of the deserializer
     */
    public <T> void addDeserializer(Class<T> type, JsonDeserializer<? extends T> deserializer) {
        module.addDeserializer(type, deserializer);
    }

    /*
    Static utility functions below
     */

    public static <T extends Plugin> T getRequiredPlugin(String pluginName, boolean requireEnabled)
            throws LoadFailureException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if(plugin != null) {
            if(plugin.isEnabled() || !requireEnabled) {
                try {
                    //noinspection unchecked
                    return (T)plugin;
                }
                catch (ClassCastException ignored) {
                    throw new LoadFailureException(String.format("ClassCastException when loading plugin %s.", pluginName));
                }
            }
            else {
                throw new LoadFailureException(String.format("Plugin %s is not enabled.", pluginName));
            }
        }
        else {
            throw new LoadFailureException(String.format("Required plugin %s cannot be found.", pluginName));
        }
    }

    public void sendPacketToPlayer(Plugin plugin, Player player, PacketContainer packetContainer) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            plugin.getLogger().log(
                    Level.WARNING,
                    String.format(
                            "Error sending packet of type '%s' to player '%s'",
                            packetContainer.getType().name(),
                            player.getName()
                    )
            );
        }
    }

    public void sendPacketToPlayer(Player player, PacketContainer packetContainer) {
        sendPacketToPlayer(this, player, packetContainer);
    }

    public void applyDefaultStage(ArenaPlayer player) {
        player.applyConditionFor("lobby", "default");
    }

    @EventHandler
    private void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ArenaPlayer arenaPlayer = new ArenaPlayer(player);
        players.put(player.getUniqueId(), arenaPlayer);

        arenaPlayer.registerCondition("lobby", "default", lobby);
    }


    /**
     * Logs a message with this plugin, at the specified level.
     * @param level The level to log at
     * @param message The log message
     */
    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    /**
     * Logs a message with this plugin at Level.INFO — to signify that things are happening which are normal, expected
     * operations.
     * @param message The message to log
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a message with this plugin at Level.WARNING — signifying that something happened which has potential to
     * cause issues but has not resulted in a hard crash.
     * @param message The message to log
     */
    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message with this plugin at Level.SEVERE — signifying that something happened which caused a hard crash.
     * @param message The message to log
     */
    public static void severe(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Calls the specified event for this plugin.
     * @param event The event to call
     */
    public static void callEvent(Event event) {
        instance.getServer().getPluginManager().callEvent(event);
    }
}
