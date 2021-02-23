package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.proxy.NMSProxy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemLine extends HologramLine<Material> {

    public ItemLine(LocalizationManager localizationManager, Location location) {
        super(localizationManager, location);
    }

    @Override
    protected PacketContainer createSpawnPacketContainer() {
        NMSProxy nmsProxy = getNmsProxy();

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packetContainer.getIntegers().write(0, getEntityId());
        packetContainer.getIntegers().write(1, nmsProxy.getEntityTypeId(EntityType.DROPPED_ITEM));
        packetContainer.getUUIDs().write(0, nmsProxy.randomUUID());

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
