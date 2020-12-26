package io.github.zap.zombies.game.corpse;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class Corpse {

    private final ProtocolManager protocolManager;

    private Player zombiesPlayer;

    private final String name = UUID.randomUUID().toString().substring(0, 16);

    private final int id;

    public Corpse(Player player) {
        this.zombiesPlayer = player;

        protocolManager = ProtocolLibrary.getProtocolManager();
        id = Zombies.getInstance().getNmsProxy().nextEntityId();

    }

    private void sendTo(Player player, PacketContainer packetContainer) {
        try {
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException exception) {
            Zombies.getInstance().getLogger()
                    .warning(String.format("Error sending a corpse packet to player %s", player.getName()));
        }
    }

    private void sendToAll(PacketContainer packetContainer) {
        for (Player player : zombiesPlayer.getWorld().getPlayers()) {
            sendTo(player, packetContainer);
        }
    }

    public void spawnDeadBody() {
        sendToAll(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        sendToAll(createSpawnPlayerPacketContainer());
        sendToAll(createSleepingPacketContainer());
        // sendToAll(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER));
    }

    private PacketContainer createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);
        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(
                new PlayerInfoData(
                        WrappedGameProfile.fromPlayer(zombiesPlayer.getPlayer()).withName(name),
                        0,
                        EnumWrappers.NativeGameMode.NOT_SET,
                        WrappedChatComponent.fromText(name))
                )
        );

        return packetContainer;
    }

    private PacketContainer createSpawnPlayerPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getUUIDs().write(0, zombiesPlayer.getUniqueId());

        Location location = zombiesPlayer.getLocation();
        packetContainer.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return packetContainer;
    }

    private PacketContainer createSleepingPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        Object nmsPose = EnumWrappers.EntityPose.SLEEPING.toNms();
        wrappedDataWatcher.setObject(6, WrappedDataWatcher.Registry.get(nmsPose.getClass()), nmsPose);

        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return packetContainer;
    }

}
