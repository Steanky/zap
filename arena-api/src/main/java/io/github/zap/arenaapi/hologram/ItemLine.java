package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.nms.common.NMSBridge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A hologram line represented by a dropped item
 */
public class ItemLine extends HologramLine<Material> {

    public ItemLine(Location location) {
        super(location);
    }

    @Override
    protected PacketContainer createSpawnPacketContainer() {
        NMSBridge nmsBridge = getBridge();

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packetContainer.getIntegers().write(0, getEntityId());
        packetContainer.getIntegers().write(1, nmsBridge.entityBridge().getEntityTypeID(EntityType.DROPPED_ITEM));
        packetContainer.getUUIDs().write(0, nmsBridge.entityBridge().randomUUID());

        Location location = getLocation();

        packetContainer.getDoubles().write(0, location.getX());
        packetContainer.getDoubles().write(1, location.getY());
        packetContainer.getDoubles().write(2, location.getZ());

        return packetContainer;
    }

    @Override
    public void updateVisualForPlayer(Player player) {
        Material material = getVisualForPlayer(player);
        PacketContainer itemUpdatePacketContainer = createItemUpdatePacket(material);

        getArenaApi().sendPacketToPlayer(player, itemUpdatePacketContainer);
    }

    private PacketContainer createItemUpdatePacket(Material material) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, getEntityId());

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer itemSerializer =
                WrappedDataWatcher.Registry.getItemStackSerializer(false);
        WrappedDataWatcher.WrappedDataWatcherObject item =
                new WrappedDataWatcher.WrappedDataWatcherObject(7, itemSerializer);

        wrappedDataWatcher.setObject(item, new ItemStack(material));

        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return packetContainer;
    }



}
