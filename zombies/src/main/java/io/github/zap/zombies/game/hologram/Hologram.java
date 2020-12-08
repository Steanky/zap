package io.github.zap.zombies.game.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.NMSUtilProxy;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Hologram {

    private final static double LINE_SPACE = 0.25;

    private final static List<Hologram> HOLOGRAMS = new ArrayList<>();

    private final ProtocolManager protocolManager;

    private final NMSUtilProxy nmsUtilProxy;

    private final Location location;

    private final List<Integer> hologramLines = new ArrayList<>();

    private final List<String> defaultLines = new ArrayList<>();

    private boolean active = true;

    static {
        Zombies plugin = Zombies.getInstance();
        ProtocolManager protocolManager = plugin.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packetContainer = event.getPacket();

                    if (packetContainer.getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.INTERACT_AT) {
                        int id = packetContainer.getIntegers().read(0);

                        for (Hologram hologram : HOLOGRAMS) {
                            if (hologram.hologramLines.contains(id)) {
                                event.setCancelled(true);

                                PacketContainer fakePacketContainer = new PacketContainer(PacketType.Play.Client.BLOCK_PLACE);
                                fakePacketContainer.getHands().write(0, packetContainer.getHands().read(0));

                                try {
                                    protocolManager.recieveClientPacket(event.getPlayer(), fakePacketContainer);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    plugin.getLogger().warning("Error blocking player interact at entity with id " + id + ":\n" + e.getMessage());
                                }

                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public Hologram(Location location) {
        Zombies plugin = Zombies.getInstance();
        protocolManager = plugin.getProtocolManager();
        nmsUtilProxy = plugin.getNmsUtilProxy();

        this.location = location;

        HOLOGRAMS.add(this);
    }

    public Hologram(Location location, int lineCount) {
        this(location);
        addLines(lineCount);
    }

    public Hologram(Location location, String... defaultLines) {
        this(location, defaultLines.length);

        for (int i = 0; i < defaultLines.length; i++) {
            setLine(i, defaultLines[i]);
            this.defaultLines.add(defaultLines[i]);
        }
    }

    /**
     * Creates multiple empty lines
     * @param lineCount The number of lines
     */
    public void addLines(int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            addLine();
        }
    }

    /**
     * Creates an empty line
     */
    public void addLine() {
        PacketContainer packetContainer = createHologramLine(location.clone().subtract(0D, hologramLines.size() * LINE_SPACE, 0D));
        sendToAll(packetContainer);
    }

    /**
     * Sets the text of a line for a player
     * @param player The player in question
     * @param index The index of the line to edit
     * @param line The new message
     */
    public void setLineFor(Player player, int index, String line) {
        PacketContainer packetContainer = createSetLinePacket(index, line);
        if (packetContainer != null) {
            sendTo(player, packetContainer);
        }
    }

    /**
     * Sets the text of a line
     * @param index The index of the line to edit
     * @param line The new message
     */
    public void setLine(int index, String line) {
        PacketContainer packetContainer = createSetLinePacket(index, line);
        if (packetContainer != null) {
            sendToAll(packetContainer);
        }
    }

    private PacketContainer createSetLinePacket(int index, String line) {
        // If index within bounds, return a packet which sets the hologram line, else return null
        return (0 <= index && index < hologramLines.size()) ? setHologramLine(hologramLines.get(index), line) : null;
    }

    /**
     * Removes all hologram entities
     */
    public void destroy() {
        PacketContainer packetContainer = removeHologramLines();
        sendToAll(packetContainer);

        hologramLines.clear();
        active = false;
        HOLOGRAMS.remove(this);
    }

    /**
     * Renders a hologram for a player
     * @param player The player to render the hologram to
     */
    public void renderTo(Player player) {
        for (int i = 0; i < defaultLines.size(); i++) {
            setLineFor(player, i, defaultLines.get(i));
        }
    }

    /**
     * Sends a packet to all players in the world
     * @param packetContainer The packet to send
     */
    private void sendToAll(PacketContainer packetContainer) {
        for (Player player : location.getWorld().getPlayers()) {
            sendTo(player, packetContainer);
        }
    }

    /**
     * Sends a packet to a player
     * @param player The player to send to
     * @param packetContainer The packet to send
     */
    private void sendTo(Player player, PacketContainer packetContainer) {
        if (active) {
            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException exception) {
                Zombies.getInstance().getLogger().warning("Error sending packet of type: " + packetContainer.getType().name() + " to player " + player.getName());
            }
        }
    }

    /**
     * Creates a packet which spawns a hologram line
     * @param location The location to spawn the hologram at
     * @return The packet
     */
    private PacketContainer createHologramLine(Location location) {
        int id = nmsUtilProxy.nextEntityId();

        hologramLines.add(id);

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getIntegers().write(1, nmsUtilProxy.getEntityLivingTypeId(EntityType.ARMOR_STAND));
        packetContainer.getUUIDs().write(0, nmsUtilProxy.randomUUID());

        packetContainer.getDoubles().write(0, location.getX());
        packetContainer.getDoubles().write(1, location.getY());
        packetContainer.getDoubles().write(2, location.getZ());

        return packetContainer;
    }

    /**
     * Creates a packet which sets the content of a hologram line
     * @param id The entity id of the hologram line
     * @param line The new hologram line message
     * @return The packet
     */
    private PacketContainer setHologramLine(int id, String line) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer invisibleSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject invisible = new WrappedDataWatcher.WrappedDataWatcherObject(0, invisibleSerializer);

        WrappedDataWatcher.Serializer customNameSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
        WrappedDataWatcher.WrappedDataWatcherObject customName = new WrappedDataWatcher.WrappedDataWatcherObject(2, customNameSerializer);
        Optional<?> opt = Optional.of(WrappedChatComponent.fromText(line).getHandle());

        WrappedDataWatcher.Serializer customNameVisibleSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.WrappedDataWatcherObject customNameVisible = new WrappedDataWatcher.WrappedDataWatcherObject(3, customNameVisibleSerializer);

        wrappedDataWatcher.setObject(invisible, (byte) 0x20);
        wrappedDataWatcher.setObject(customName, opt);
        wrappedDataWatcher.setObject(customNameVisible, true);
        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return packetContainer;
    }

    /**
     * Creates a packet which removes all hologram entities
     * @return The packet
     */
    private PacketContainer removeHologramLines() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, hologramLines.stream().mapToInt(Integer::intValue).toArray());

        return packetContainer;
    }


}
