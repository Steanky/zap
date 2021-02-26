package io.github.zap.zombies.game.corpse;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.zombies.Zombies;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class Corpse {

    private final ProtocolManager protocolManager;

    private final Player zombiesPlayer;

    private final UUID uniqueId = UUID.randomUUID();

    private final int id;

    static {
        Zombies zombies = Zombies.getInstance();
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(zombies, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                zombies.getLogger().info(event.getPacket().getPlayerInfoAction().read(0).name());
            }
        });
    }

    public Corpse(Player player) {
        this.zombiesPlayer = player;

        protocolManager = ProtocolLibrary.getProtocolManager();
        id = Zombies.getInstance().getNmsProxy().nextEntityId();
        spawnDeadBody();
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
        /*new BukkitRunnable() {

            @Override
            public void run() {
                sendToAll(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER));
            }
        }.runTaskLater(Zombies.getInstance(), 1L);*/
    }

    private PacketContainer createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);
        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(
                new PlayerInfoData(
                        /* WrappedGameProfile.fromPlayer(zombiesPlayer.getPlayer())
                                // .withId(uniqueId.toString())
                                .withName(uniqueId.toString().substring(0, 16)),
                                */
                        new WrappedGameProfile(uniqueId, uniqueId.toString().substring(0, 16)),
                        0,
                        EnumWrappers.NativeGameMode.NOT_SET,
                        WrappedChatComponent.fromText(uniqueId.toString().substring(0, 16))
                ))
        );

        return packetContainer;
    }

    private PacketContainer createSpawnPlayerPacketContainer() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getUUIDs().write(0, uniqueId);

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
