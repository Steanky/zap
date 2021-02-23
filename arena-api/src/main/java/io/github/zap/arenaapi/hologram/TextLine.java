package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.proxy.NMSProxy;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TextLine extends HologramLine<ImmutablePair<String, String[]>> {

    public TextLine(LocalizationManager localizationManager, Location location) {
        super(localizationManager, location);
    }

    @Override
    protected PacketContainer createSpawnPacketContainer() {
        NMSProxy nmsProxy = getNmsProxy();

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packetContainer.getIntegers().write(0, getEntityId());
        packetContainer.getIntegers().write(1, nmsProxy.getEntityTypeId(EntityType.ARMOR_STAND));
        packetContainer.getUUIDs().write(0, nmsProxy.randomUUID());

        Location location = getLocation();

        packetContainer.getDoubles().write(0, location.getX());
        packetContainer.getDoubles().write(1, location.getY());
        packetContainer.getDoubles().write(2, location.getZ());

        return packetContainer;
    }

    @Override
    public void updateVisualForPlayer(Player player) {
        ImmutablePair<String, String[]> visual = getVisualForPlayer(player);
        String message = String.format(
                getLocalizationManager().getLocalizedMessageFor(player, visual.getLeft()),
                (Object[]) visual.getRight()
        );
        PacketContainer lineUpdatePacketContainer = createLineUpdatePacket(message);

        getArenaApi().sendPacketToPlayer(player, lineUpdatePacketContainer);
    }

    private PacketContainer createLineUpdatePacket(String line) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, getEntityId());

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer invisibleSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject invisible =
                new WrappedDataWatcher.WrappedDataWatcherObject(0, invisibleSerializer);

        WrappedDataWatcher.Serializer customNameSerializer =
                WrappedDataWatcher.Registry.getChatComponentSerializer(true);
        WrappedDataWatcher.WrappedDataWatcherObject customName =
                new WrappedDataWatcher.WrappedDataWatcherObject(2, customNameSerializer);
        Optional<?> opt = Optional.of(WrappedChatComponent.fromText(line).getHandle());

        WrappedDataWatcher.Serializer customNameVisibleSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.WrappedDataWatcherObject customNameVisible =
                new WrappedDataWatcher.WrappedDataWatcherObject(3, customNameVisibleSerializer);

        wrappedDataWatcher.setObject(invisible, (byte) 0x20);
        wrappedDataWatcher.setObject(customName, opt);
        wrappedDataWatcher.setObject(customNameVisible, true);
        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return packetContainer;
    }

}
