package io.github.zap.zombies.game.corpse;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.arena.ManagingArena;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.perk.FastRevive;
import io.github.zap.zombies.game.perk.PerkType;
import io.github.zap.zombies.proxy.ZombiesNMSProxy;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Represents the corpse of a knocked down or dead player
 */
public class Corpse {

    private final ProtocolManager protocolManager;

    private final ZombiesNMSProxy nmsProxy;

    private final ZombiesPlayer zombiesPlayer;

    @Getter
    private final Location location;

    private final UUID uniqueId = UUID.randomUUID();

    private final int id;

    private final Hologram hologram;

    @Getter
    private final PacketContainer addCorpseToTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

    private final int defaultDeathTime;

    private int deathTaskId;

    private int deathTime;

    @Getter
    private boolean active = true;

    private ZombiesPlayer reviver;

    private int reviveTime;

    public Corpse(ZombiesPlayer zombiesPlayer) {
        this.nmsProxy = Zombies.getInstance().getNmsProxy();
        this.zombiesPlayer = zombiesPlayer;
        this.location = zombiesPlayer.getPlayer().getLocation();
        this.defaultDeathTime = zombiesPlayer.getArena().getMap().getCorpseDeathTime();
        this.hologram =
                new Hologram(location.clone().add(0, 2, 0));
        this.deathTime = defaultDeathTime;

        hologram.addLine("----------------------------------");
        hologram.addLine(ChatColor.RED + "help this noob");

        hologram.addLine(String.format("%s%fs", ChatColor.RED, convertTicksToSeconds(defaultDeathTime)));
        hologram.addLine("----------------------------------");

        ZombiesArena zombiesArena = zombiesPlayer.getArena();
        zombiesArena.getCorpses().add(this);
        zombiesArena.getAvailableCorpses().add(this);
        zombiesArena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);

        protocolManager = ProtocolLibrary.getProtocolManager();
        id = this.nmsProxy.nextEntityId();

        spawnDeadBody();
        startDying();
    }

    /**
     * Sets the current reviver of the corpse
     * @param reviver The reviver of the corpse
     */
    public void setReviver(ZombiesPlayer reviver) {
        if (reviver == null) {
            zombiesPlayer.getArena().getAvailableCorpses().add(this);
            deathTime = defaultDeathTime;
            startDying();
        } else {
            if (deathTaskId != -1) {
                Bukkit.getScheduler().cancelTask(deathTaskId);
            }

            this.reviveTime = ((FastRevive) reviver.getPerks().getPerk(PerkType.FAST_REVIVE)).getReviveTime();
            hologram.updateLine(1, ChatColor.RED + "Reviving...");
        }

        this.reviver = reviver;
    }

    /**
     * Removes 0.1s of revival time from the corpse
     */
    public void continueReviving() {
        if (reviveTime == 0) {
            active = false;
            zombiesPlayer.revive();
        } else {
            hologram.updateLine(2, String.format("%s%fs", ChatColor.RED, convertTicksToSeconds(reviveTime)));
            reviveTime--;
        }
    }

    private void startDying() {
        hologram.updateLine(1, "help this noob");

        deathTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Zombies.getInstance(),
                this::continueDying,
                0L,
                2L
        );
    }

    /**
     * Removes 0.1s of death time from the corpse
     */
    public void continueDying() {
        if (deathTime == 0) {
            zombiesPlayer.kill();
            zombiesPlayer.getArena().getAvailableCorpses().remove(this);
        } else {
            hologram.updateLine(2, String.format("%s%fs", ChatColor.RED, convertTicksToSeconds(deathTime)));
            deathTime -= 1;
        }
    }

    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        for (Player player : playerListArgs.getPlayers()) {
            spawnDeadBodyForPlayer(player);
        }
    }

    private double convertTicksToSeconds(int ticks) {
        return (double) (ticks / 20) + 0.05D * ticks % 20;
    }

    private void sendPacketToPlayer(PacketContainer packetContainer, Player player) {
        ArenaApi.getInstance().sendPacketToPlayer(Zombies.getInstance(), player, packetContainer);
    }

    private void sendPacket(PacketContainer packetContainer) {
        for (Player player : zombiesPlayer.getPlayer().getWorld().getPlayers()) {
            sendPacketToPlayer(packetContainer, player);
        }
    }

    private void spawnDeadBody() {
        for (Player player : zombiesPlayer.getPlayer().getWorld().getPlayers()) {
            spawnDeadBodyForPlayer(player);
        }
    }

    private void spawnDeadBodyForPlayer(Player player) {
        sendPacketToPlayer(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.ADD_PLAYER), player);
        sendPacketToPlayer(createSpawnPlayerPacketContainer(), player);
        sendPacketToPlayer(createSleepingPacketContainer(), player);

        addCorpseToTeamPacket.getStrings().write(0, zombiesPlayer.getArena().getCorpseTeamName());
        addCorpseToTeamPacket.getIntegers().write(0, 3);
        addCorpseToTeamPacket.getSpecificModifier(Collection.class)
                .write(0, Collections.singletonList(uniqueId.toString().substring(0, 16)));

        sendPacketToPlayer(addCorpseToTeamPacket, player);

        //sendPacketToPlayer(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER), player);
    }

    private PacketContainer createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uniqueId, uniqueId.toString().substring(0, 16));
        wrappedGameProfile.getProperties().put("textures", nmsProxy.getSkin(zombiesPlayer.getPlayer()));

        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(
                new PlayerInfoData(
                        wrappedGameProfile,
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

        Location location = zombiesPlayer.getPlayer().getLocation();
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

    /**
     * Destroys the corpse and removes its trace from the arena it is in
     */
    public void destroy() {
        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[] { id });

        for (Player player : zombiesPlayer.getPlayer().getWorld().getPlayers()) {
            sendPacketToPlayer(killPacketContainer, player);
        }

        zombiesPlayer.getArena().getCorpses().remove(this);
        zombiesPlayer.getArena().getAvailableCorpses().remove(this);
        zombiesPlayer.getArena().getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Corpse corpse = (Corpse) o;

        return uniqueId.equals(corpse.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
