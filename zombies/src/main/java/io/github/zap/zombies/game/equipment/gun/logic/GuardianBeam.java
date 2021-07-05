package io.github.zap.zombies.game.equipment.gun.logic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.util.AttributeHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gun beam which freezes zombies
 */
public class GuardianBeam extends BasicBeam {

    private final static String GUARDIAN_BEAM_ATTRIBUTE_NAME = "guardian_beam_freeze";

    private final static Map<Mob, UUID> mobFreezeUUIDMap = new HashMap<>();

    private final int freezeTime;

    private final int guardianId;

    private final int armorStandId;

    public GuardianBeam(MapData mapData, ZombiesPlayer zombiesPlayer, Location root, GuardianGunLevel level) {
        super(mapData, zombiesPlayer, root, level);
        this.freezeTime = level.getFreezeTime();

        ZombiesNMSProxy zombiesNMSProxy = Zombies.getInstance().getNmsProxy();
        guardianId = zombiesNMSProxy.nextEntityId();
        armorStandId = zombiesNMSProxy.nextEntityId();
    }

    @Override
    public void send() {
        PacketContainer guardianSpawnPacket = getGuardianSpawnPacket();
        PacketContainer armorStandSpawnPacket = getArmorStandSpawnPacket();
        PacketContainer guardianMetadataPacket = getGuardianMetadataPacket();
        PacketContainer armorStandMetadataPacket = getArmorStandMetadataPacket();
        PacketContainer killPacketContainer = getKillPacket();

        ArenaApi arenaApi = ArenaApi.getInstance();
        Zombies zombies = Zombies.getInstance();
        ZombiesArena zombiesArena = getZombiesPlayer().getArena();
        World world = zombiesArena.getWorld();

        for (Player player : world.getPlayers()) {
            arenaApi.sendPacketToPlayer(zombies, player, guardianSpawnPacket);
            arenaApi.sendPacketToPlayer(zombies, player, armorStandSpawnPacket);
            arenaApi.sendPacketToPlayer(zombies, player, guardianMetadataPacket);
            arenaApi.sendPacketToPlayer(zombies, player, armorStandMetadataPacket);
        }

        zombiesArena.runTaskLater(5L, () -> {
            for (Player player : world.getPlayers()) {
                arenaApi.sendPacketToPlayer(zombies, player, killPacketContainer);
            }
        });

        super.send();
    }

    private PacketContainer getGuardianSpawnPacket() {
        ZombiesNMSProxy zombiesNMSProxy = Zombies.getInstance().getNmsProxy();
        Vector location = getRoot();

        PacketContainer spawnGuardianPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        spawnGuardianPacket.getIntegers()
                .write(0, guardianId)
                .write(1, zombiesNMSProxy.getEntityTypeId(EntityType.GUARDIAN));
        spawnGuardianPacket.getUUIDs().write(0, zombiesNMSProxy.randomUUID());
        spawnGuardianPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return spawnGuardianPacket;
    }

    private PacketContainer getArmorStandSpawnPacket() {
        ZombiesNMSProxy zombiesNMSProxy = Zombies.getInstance().getNmsProxy();
        Vector location = getRoot().clone();

        location.add(getDirectionVector().clone().normalize().multiply(getDistance()));

        PacketContainer spawnArmorStandPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        spawnArmorStandPacket.getIntegers()
                .write(0, armorStandId)
                .write(1, zombiesNMSProxy.getEntityTypeId(EntityType.ARMOR_STAND));
        spawnArmorStandPacket.getUUIDs().write(0, zombiesNMSProxy.randomUUID());
        spawnArmorStandPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return spawnArmorStandPacket;
    }

    private PacketContainer getGuardianMetadataPacket() {
        PacketContainer guardianMetadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        guardianMetadataPacket.getIntegers().write(0, guardianId);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer invisibleSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject invisible
                = new WrappedDataWatcher.WrappedDataWatcherObject(0, invisibleSerializer);

        WrappedDataWatcher.Serializer targetSerializer = WrappedDataWatcher.Registry.get(Integer.class);
        WrappedDataWatcher.WrappedDataWatcherObject target
                = new WrappedDataWatcher.WrappedDataWatcherObject(16, targetSerializer);

        wrappedDataWatcher.setObject(invisible, (byte) 0x20);
        wrappedDataWatcher.setObject(target, armorStandId);

        guardianMetadataPacket.getWatchableCollectionModifier()
                .write(0, wrappedDataWatcher.getWatchableObjects());

        return guardianMetadataPacket;
    }

    private PacketContainer getArmorStandMetadataPacket() {
        PacketContainer armorStandMetadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        armorStandMetadataPacket.getIntegers().write(0, armorStandId);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer invisibleSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject invisible
                = new WrappedDataWatcher.WrappedDataWatcherObject(0, invisibleSerializer);

        wrappedDataWatcher.setObject(invisible, (byte) 0x20);

        armorStandMetadataPacket.getWatchableCollectionModifier()
                .write(0, wrappedDataWatcher.getWatchableObjects());

        return armorStandMetadataPacket;
    }

    private PacketContainer getKillPacket() {
        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[] { guardianId, armorStandId });

        return killPacketContainer;
    }

    @Override
    protected void damageEntity(RayTraceResult rayTraceResult) {
        super.damageEntity(rayTraceResult);
        Mob mob = (Mob) rayTraceResult.getHitEntity();

        if (mob != null) {
            AttributeInstance speed = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

            if (speed != null) {
                ZombiesArena zombiesArena = getZombiesPlayer().getArena();
                AttributeModifier attributeModifier = AttributeHelper
                        .getModifier(speed, GUARDIAN_BEAM_ATTRIBUTE_NAME)
                        .orElseGet(() -> new AttributeModifier(
                                GUARDIAN_BEAM_ATTRIBUTE_NAME,
                                0L,
                                AttributeModifier.Operation.MULTIPLY_SCALAR_1
                        ));

                UUID freezeUUID = UUID.randomUUID();
                mobFreezeUUIDMap.put(mob, freezeUUID);
                zombiesArena.runTaskLater(
                        freezeTime,
                        () -> {
                            if (freezeUUID.equals(mobFreezeUUIDMap.get(mob))) {
                                speed.removeModifier(attributeModifier);
                                mobFreezeUUIDMap.remove(mob);
                            }
                        }
                );
            }
        }
    }
}
