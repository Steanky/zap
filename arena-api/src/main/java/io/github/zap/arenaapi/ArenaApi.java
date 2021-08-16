package io.github.zap.arenaapi;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.arenaapi.nms.common.ArenaNMSBridge;
import io.github.zap.arenaapi.nms.v1_16_R3.ArenaNMSBridge_v1_16_R3;
import io.github.zap.arenaapi.serialize.*;
import io.github.zap.party.PartyPlusPlus;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class ArenaApi extends JavaPlugin implements Listener {
    @Getter
    private static ArenaApi instance;

    @Getter
    private ArenaNMSBridge nmsBridge;

    @Getter
    private PartyPlusPlus partyPlusPlus;

    @Getter
    private ProtocolLib protocolLib;

    @Getter
    private SimpleModule module;

    @Getter
    private ObjectMapper mapper;

    private final Map<String, ArenaManager<?>> arenaManagers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        StopWatch timer = StopWatch.createStarted();

        try {
            initBridge();
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

    @Override
    public void onDisable() {
        for(ArenaManager<?> manager : arenaManagers.values()) {
            manager.dispose();
        }

        info("Disabled successfully.");
    }

    private void initBridge() throws LoadFailureException {
        nmsBridge = ArenaNMSBridge.selectBridge(ArenaNMSBridge_v1_16_R3.INSTANCE);

        if(nmsBridge == null) {
            throw new LoadFailureException(String.format("Unsupported NMS package version '%s'.", ArenaNMSBridge.CURRENT_NMS_VERSION));
        }
    }

    private void initDependencies() throws LoadFailureException {
        protocolLib = getDependentPlugin(PluginNames.PROTOCOL_LIB, true,true);
        partyPlusPlus = ArenaApi.getDependentPlugin(PluginNames.PARTY_PLUS_PLUS, false, false);
    }

    private void initMapper() {
        module = new SimpleModule();

        module.addSerializer(Vector.class, new VectorSerializer());
        module.addDeserializer(Vector.class, new VectorDeserializer());

        module.addSerializer(BoundingBox.class, new BoundingBoxSerializer());
        module.addDeserializer(BoundingBox.class, new BoundingBoxDeserializer());

        module.addSerializer(Sound.class, new SoundSerializer());
        module.addDeserializer(Sound.class, new SoundDeserializer());

        module.addDeserializer(Color.class, new ColorDeserializer());
        module.addDeserializer(Particle.DustOptions.class, new DustOptionsDeserializer());

        module.addSerializer(Pair.class, new PairSerializer());
        module.addDeserializer(Pair.class, new PairDeserializer());

        module.addAbstractTypeMapping(Pair.class, ImmutablePair.class);
        module.addAbstractTypeMapping(Pair.class, MutablePair.class);

        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
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
            List<Player> leavingPlayers = information.getJoinable().getPlayers();

            for(Player player : leavingPlayers) {
                Arena<?> currentArena = arenaIn(player);

                if(currentArena != null) {
                    currentArena.handleLeave(Lists.newArrayList(player));
                }
            }

            arenaManager.handleJoin(information, onCompletion);
        }
        else {
            warning(String.format("Invalid JoinInformation received: '%s' is not a game.", gameName));
        }
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

    public static <T extends Plugin> T getDependentPlugin(String pluginName, boolean requireExists,
                                                          boolean requireEnabled) throws LoadFailureException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (plugin != null) {
            if (plugin.isEnabled() || !requireEnabled) {
                try {
                    //noinspection unchecked
                    return (T) plugin;
                } catch (ClassCastException ignored) {
                    throw new LoadFailureException(String.format("ClassCastException when loading plugin %s.",
                            pluginName));
                }
            } else if (requireExists) {
                throw new LoadFailureException(String.format("Plugin %s is not enabled.", pluginName));
            }
        }
        else if (requireExists) {
            throw new LoadFailureException(String.format("Required plugin %s cannot be found.", pluginName));
        }

        return null;
    }

    public void sendPacketToPlayer(Plugin plugin, Player player, PacketContainer packetContainer) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, String.format("Error sending packet of type '%s' to player '%s'",
                    packetContainer.getType().name(), player.getName()));
        }
    }

    public void sendPacketToPlayer(Player player, PacketContainer packetContainer) {
        sendPacketToPlayer(this, player, packetContainer);
    }

    public void applyDefaultCondition(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(20);
        player.setInvulnerable(true);
        player.setInvisible(false);
        player.setWalkSpeed(0.2F);
        player.setFallDistance(0);
        player.setAllowFlight(false);
        player.setCollidable(true);
        player.setFallDistance(0);
        player.setFlySpeed(0.1F);
        player.setGameMode(GameMode.ADVENTURE);
        player.setArrowsInBody(0);
        player.setLevel(0);
        player.setExp(0F);
        player.setFireTicks(0);
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            for(AttributeModifier modifier : attribute.getModifiers()) {
                attribute.removeModifier(modifier);
            }
        }
    }

    @EventHandler
    private void playerJoinEvent(PlayerJoinEvent event) {
        applyDefaultCondition(event.getPlayer());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (arenaCurrentlyIn(player) != null) {
                event.getPlayer().hidePlayer(this, player);
            }
        }
    }

    @EventHandler
    private void playerTeleportEvent(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Arena<?> arena = arenaIn(player);

        if(arena != null && !event.getTo().getWorld().equals(arena.getWorld()) && event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            arena.handleLeave(Lists.newArrayList(player));
        }
    }

    /**
     * Gets an iterator for every arena managed by this instance of ArenaApi.
     * @return An iterator that will iterate through each arena
     */
    public Iterator<? extends Arena<?>> arenaIterator() {
        return arenaManagers.values().stream().flatMap(arenaManager ->
                arenaManager.getArenas().values().stream()).iterator();
    }

    public void evacuatePlayer(@NotNull Arena<?> from, @NotNull Player player) {
        player.teleport(from.getManager().getHubLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public @Nullable Arena<?> arenaIn(@NotNull Player player) {
        Iterator<? extends Arena<?>> arenaIterator = arenaIterator();

        while(arenaIterator.hasNext()) {
            Arena<?> arena = arenaIterator.next();

            if(arena.hasPlayer(player.getUniqueId())) {
                return arena;
            }
        }

        return null;
    }

    public @Nullable Arena<?> arenaCurrentlyIn(@NotNull Player player) {
        Iterator<? extends Arena<?>> arenaIterator = arenaIterator();

        while(arenaIterator.hasNext()) {
            Arena<?> arena = arenaIterator.next();

            if(arena.isPlayerPlaying(player.getUniqueId())) {
                return arena;
            }
        }

        return null;
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
