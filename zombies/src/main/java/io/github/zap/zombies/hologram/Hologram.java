package io.github.zap.zombies.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.proxy.NMSProxy;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Hologram implements Listener {

    private final static double LINE_SPACE = 0.25;

    private final ProtocolManager protocolManager;

    private final Location location;

    private final List<Integer> lines = new ArrayList<>();

    public Hologram(Location location) {
        Zombies plugin = Zombies.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        protocolManager = plugin.getProtocolManager();

        this.location = location;
    }

    public Hologram(Location location, int lineCount) {
        this(location);
        addLines(lineCount);
    }

    public void addLines(int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            addLine();
        }
    }

    public void addLine() {
        PacketContainer packetContainer = createHologramLine(location.clone().subtract(0D, lines.size() * LINE_SPACE, 0D));
        sendToAll(packetContainer);
    }

    public void setLineFor(Player player, int index, String line) {
        if (0 <= index && index < lines.size()) {
            int id = lines.get(index);

            PacketContainer packetContainer = setHologramLine(id, line);
            sendTo(player, packetContainer);
        }
    }

    /*
    public void removeLine(int index) {
        EntityArmorStand entityArmorStand = lines.get(index);
        entityArmorStand.killEntity();

        PacketContainer packetContainer = removeHologramLine(entityArmorStand);
        sendToAll(packetContainer);

        lines.remove(index);
    }
    */

    private void sendToAll(PacketContainer packetContainer) {
        for (Player player : location.getWorld().getPlayers()) {
            sendTo(player, packetContainer);
        }
    }

    private void sendTo(Player player, PacketContainer packetContainer) {
        try {
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException exception) {
            Zombies.getInstance().getLogger().warning("Error sending packet of type: " + packetContainer.getType().name() + " to player " + player.getName());
        }
    }

    private PacketContainer createHologramLine(Location location) {
        NMSProxy nmsProxy = Zombies.getInstance().getNmsProxy();

        AtomicInteger entityCount = nmsProxy.getEntityCount();
        int id = entityCount.incrementAndGet();

        lines.add(id);

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getIntegers().write(1, 1);
        packetContainer.getUUIDs().write(0, nmsProxy.randomUUID());

        packetContainer.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return packetContainer;
    }

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

    /*
    private PacketContainer removeHologramLine(EntityArmorStand entityArmorStand) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, new int[]{entityArmorStand.getId()});

        return packetContainer;
    }
    */

}
