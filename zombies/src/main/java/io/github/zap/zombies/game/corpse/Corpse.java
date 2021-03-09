package io.github.zap.zombies.game.corpse;

import com.comphenix.protocol.PacketType;
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
import net.kyori.adventure.text.Component;
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

    private final ZombiesNMSProxy nmsProxy;

    @Getter
    private final ZombiesPlayer zombiesPlayer;

    @Getter
    private final Location location;

    private final UUID uniqueId = UUID.randomUUID();

    private final int id;

    private final Hologram hologram;

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
        this.hologram = new Hologram(location.clone().add(0, 2, 0));
        this.deathTime = defaultDeathTime;
        this.id = this.nmsProxy.nextEntityId();

        hologram.addLine(ChatColor.YELLOW + "----------------------------------");
        hologram.addLine(String.format("%shelp this noob", ChatColor.RED));

        hologram.addLine(String.format("%s%fs", ChatColor.RED, convertTicksToSeconds(defaultDeathTime)));
        hologram.addLine(ChatColor.YELLOW + "----------------------------------");

        ZombiesArena zombiesArena = zombiesPlayer.getArena();
        zombiesArena.getCorpses().add(this);
        zombiesArena.getAvailableCorpses().add(this);
        zombiesArena.getPlayerJoinEvent().registerHandler(this::onPlayerJoin);

        spawnDeadBody();
        startDying();
    }

    /**
     * Terminates the corpse's execution early.
     */
    public void terminate() {
        if (active) {
            active = false;

            if (hologram.getHologramLines().size() > 0) {
                hologram.destroy();
            }

            ZombiesArena zombiesArena = zombiesPlayer.getArena();
            zombiesArena.getCorpses().remove(this);
            zombiesArena.getAvailableCorpses().remove(this);
            zombiesArena.getPlayerJoinEvent().removeHandler(this::onPlayerJoin);

            if (deathTaskId != -1) {
                Bukkit.getScheduler().cancelTask(deathTaskId);
            }
        }
    }

    /**
     * Sets the current reviver of the corpse
     * @param reviver The reviver of the corpse
     */
    public void setReviver(ZombiesPlayer reviver) {
        if (reviver == null) {
            zombiesPlayer.getArena().getAvailableCorpses().add(this);
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
        if (reviveTime <= 0) {
            active = false;
            zombiesPlayer.revive();
            zombiesPlayer.getPlayer().sendActionBar(Component.text());
            reviver.getPlayer().sendActionBar(Component.text());
        } else {
            double timeRemaining = convertTicksToSeconds(reviveTime);
            String secondsRemainingString = String.format("%s%.1fs", ChatColor.RED, timeRemaining);
            hologram.updateLine(2, secondsRemainingString);
            zombiesPlayer.getPlayer().sendActionBar(Component.text(
                    String.format(
                            "%sYou are being revived by %s%s! %s- %s!",
                            ChatColor.RED,
                            ChatColor.YELLOW,
                            reviver.getPlayer().getName(),
                            ChatColor.WHITE,
                            secondsRemainingString)
            ));
            reviver.getPlayer().sendActionBar(Component.text(
                    String.format(
                            "%sReviving %s%s... %s- %s!",
                            ChatColor.RED,
                            ChatColor.YELLOW,
                            zombiesPlayer.getPlayer().getName(),
                            ChatColor.WHITE,
                            secondsRemainingString)
            ));

            reviveTime -= 2;
        }
    }

    private void startDying() {
        deathTime = defaultDeathTime;
        hologram.updateLine(1, ChatColor.RED + "help this noob");

        deathTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Zombies.getInstance(),
                this::continueDying,
                0L,
                2L
        );
    }

    private void continueDying() {
        if (deathTime <= 0) {
            active = false;
            zombiesPlayer.kill();
            hologram.destroy();
            zombiesPlayer.getArena().getAvailableCorpses().remove(this);
            zombiesPlayer.getPlayer().sendActionBar(Component.text());
        } else {
            double timeRemaining = convertTicksToSeconds(deathTime);
            String secondsRemainingString = String.format("%s%.1fs", ChatColor.RED, timeRemaining);
            hologram.updateLine(2, secondsRemainingString);
            zombiesPlayer.getPlayer().sendActionBar(Component.text(
                    String.format("%sYou will die in %s%s!", ChatColor.RED, ChatColor.YELLOW, secondsRemainingString)
            ));
            deathTime -= 2;
        }
    }

    private void onPlayerJoin(ManagingArena.PlayerListArgs playerListArgs) {
        for (Player player : playerListArgs.getPlayers()) {
            spawnDeadBodyForPlayer(player);
            hologram.renderToPlayer(player);
        }
    }

    private double convertTicksToSeconds(int ticks) {
        return (double) (ticks / 20) + 0.05D * (ticks % 20);
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

        PacketContainer addCorpseToTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        addCorpseToTeamPacket.getStrings().write(0, zombiesPlayer.getArena().getCorpseTeamName());
        addCorpseToTeamPacket.getIntegers().write(0, 3);
        addCorpseToTeamPacket.getSpecificModifier(Collection.class)
                .write(0, Collections.singletonList(uniqueId.toString().substring(0, 16)));

        sendPacketToPlayer(addCorpseToTeamPacket, player);

        Bukkit.getScheduler().runTaskLater(Zombies.getInstance(), ()->
                sendPacketToPlayer(createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER), player), 1);
    }

    private PacketContainer createPlayerInfoPacketContainer(EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uniqueId, uniqueId.toString().substring(0, 16));
        WrappedSignedProperty skin = nmsProxy.getSkin(zombiesPlayer.getPlayer());
        if (skin != null) {
            wrappedGameProfile.getProperties().put("textures", nmsProxy.getSkin(zombiesPlayer.getPlayer()));
        }

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
        PacketContainer removeCorpseFromTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        removeCorpseFromTeamPacket.getStrings().write(0, zombiesPlayer.getArena().getCorpseTeamName());
        removeCorpseFromTeamPacket.getIntegers().write(0, 4);
        removeCorpseFromTeamPacket.getSpecificModifier(Collection.class)
                .write(0, Collections.singletonList(uniqueId.toString().substring(0, 16)));

        sendPacket(removeCorpseFromTeamPacket);

        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[] { id });

        sendPacket(killPacketContainer);
        terminate();

        //if (hologram.getHologramLines().size() > 0) {
        //    hologram.destroy();
        //}

        //zombiesPlayer.getArena().getCorpses().remove(this);
        //zombiesPlayer.getArena().getAvailableCorpses().remove(this);
        //zombiesPlayer.getArena().getPlayerJoinEvent().removeHandler(this::onPlayerJoin);
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
