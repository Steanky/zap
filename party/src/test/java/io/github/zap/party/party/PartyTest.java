package io.github.zap.party.party;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PartyTest {

    private final static UUID VERYAVERAGE_UUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    private final static UUID BIGDIP_UUID = UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692");

    private Plugin plugin;

    private Server server;

    private BukkitScheduler scheduler;

    private Player owner;

    private Player member;

    @BeforeEach
    public void setup() {
        this.plugin = Mockito.mock(Plugin.class);
        this.server = Mockito.mock(Server.class);
        this.scheduler = Mockito.mock(BukkitScheduler.class);

        Mockito.when(this.plugin.getServer()).thenReturn(this.server);
        Mockito.when(this.server.getScheduler()).thenReturn(this.scheduler);

        this.owner = Mockito.mock(Player.class);
        Mockito.when(this.owner.getUniqueId()).thenReturn(VERYAVERAGE_UUID);
        Mockito.when(this.owner.displayName()).thenReturn(Component.text("VeryAverage"));

        this.member = Mockito.mock(Player.class);
        Mockito.when(this.member.getUniqueId()).thenReturn(BIGDIP_UUID);
        Mockito.when(this.member.displayName()).thenReturn(Component.text("BigDip123"));
    }

    @Test
    public void testBroadcastMessage() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.addMember(this.member);

        Component component = Component.text("Hello, World!");
        party.broadcastMessage(component);

        for (PartyMember partyMember : party.getMembers()) {
            partyMember.getPlayerIfOnline()
                    .ifPresent(player -> Mockito.verify(player).sendMessage(ArgumentMatchers.eq(component)));
        }
    }

    @Test
    public void testTransferToPlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        Optional<PartyMember> newOwner = party.addMember(this.member);
        Assertions.assertTrue(newOwner.isPresent());

        party.transferPartyToPlayer(this.member);

        Optional<PartyMember> owner = party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(owner.get(), newOwner.get());
    }

    @Test
    public void testTransferToPlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        PartyMember oldOwner = new PartyMember(this.owner);
        Party party = new Party(this.plugin, MiniMessage.get(), oldOwner, new PartySettings(), PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.transferPartyToPlayer(this.member);

        Optional<PartyMember> owner = party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(oldOwner, owner.get());
    }

    @Test
    public void testInvitePlayerNotInPartyWithoutExpiration() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.invitePlayer(this.member, this.owner);
        Assertions.assertEquals(1, party.getInvites().size());

        Collection<PartyMember> initialMembers = party.getMembers();
        party.addMember(this.member);

        Mockito.verify(this.scheduler).runTaskLater(ArgumentMatchers.eq(this.plugin),
                ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()));
        Mockito.verify(this.scheduler).cancelTask(taskId);
        for (PartyMember partyMember : initialMembers) {
            Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
            Assertions.assertTrue(playerOptional.isPresent());
            Player player = playerOptional.get();
            Mockito.verify(player, Mockito.times(2))
                    .sendMessage(ArgumentMatchers.any(Component.class));
        }
        Mockito.verify(this.member, Mockito.times(2))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testInvitePlayerNotInPartyWithExpiration() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Runnable[] runnable = new Runnable[1];
        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime())))
                .then((Answer<BukkitTask>) invocation -> {
                    runnable[0] = invocation.getArgument(1);
                    return bukkitTask;
                });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.invitePlayer(this.member, this.owner);
        Assertions.assertEquals(1, party.getInvites().size());
        runnable[0].run();
        Assertions.assertEquals(0, party.getInvites().size());

        Collection<PartyMember> initialMembers = party.getMembers();

        Mockito.verify(this.scheduler).runTaskLater(ArgumentMatchers.eq(this.plugin),
                ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()));
        Mockito.verify(this.scheduler, Mockito.times(0)).cancelTask(taskId);
        for (PartyMember partyMember : initialMembers) {
            Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
            Assertions.assertTrue(playerOptional.isPresent());
            Player player = playerOptional.get();
            Mockito.verify(player, Mockito.times(2))
                    .sendMessage(ArgumentMatchers.any(Component.class));
        }
        Mockito.verify(this.member, Mockito.times(2))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testInviteRecreatedPlayer() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.invitePlayer(this.member, this.owner);

        OfflinePlayer reborn = Mockito.mock(OfflinePlayer.class);
        Mockito.when(reborn.getPlayer()).thenReturn(null);
        Mockito.when(reborn.isOnline()).thenReturn(false);
        Mockito.when(reborn.getUniqueId()).thenReturn(BIGDIP_UUID);

        Assertions.assertTrue(party.hasInvite(reborn));
    }

    @Test
    public void testAddPlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        int[] playersAdded = new int[] { 0 };
        party.registerJoinHandler(player -> {
            Assertions.assertTrue(party.hasMember(player.getOfflinePlayer()));
            playersAdded[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        int initialSize = party.getMembers().size();
        party.invitePlayer(this.member, this.owner);
        Optional<PartyMember> newMember = party.addMember(this.member);
        Assertions.assertEquals(0, party.getInvites().size());

        Assertions.assertEquals(1, playersAdded[0]);
        Assertions.assertEquals(initialSize + 1, party.getMembers().size());
        Assertions.assertTrue(newMember.isPresent());
    }

    @Test
    public void testAddPlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.addMember(this.member);

        int initialSize = party.getMembers().size();
        party.registerJoinHandler(player -> Assertions.fail("No player join handler should be called."));
        Optional<PartyMember> newMember = party.addMember(this.member);

        Assertions.assertEquals(initialSize, party.getMembers().size());
        Assertions.assertTrue(newMember.isEmpty());
    }

    @Test
    public void testRemovePlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        party.registerLeaveHandler(player -> Assertions.fail("No player leave handler should be called."));

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        int initialSize = party.getMembers().size();
        party.removeMember(this.member, false);

        Assertions.assertEquals(initialSize, party.getMembers().size());
    }

    @Test
    public void testRemovePlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(this.member).sendMessage(ArgumentMatchers.any(Component.class));
        party.addMember(this.member);

        freeze[0] = true;
        int initialSize = party.getMembers().size();
        party.removeMember(this.member, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());
        Assertions.assertTrue(party.getMember(this.member).isEmpty());
        Mockito.verify(this.member, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test // a bit strange but meh
    public void testRemoveOwnerWithAnotherMember() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(this.owner).sendMessage(ArgumentMatchers.any(Component.class));
        party.addMember(this.member);

        freeze[0] = true;
        int initialSize = party.getMembers().size();
        party.removeMember(this.owner, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(party.getMember(this.owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());

        Optional<PartyMember> ownerOptional = party.getOwner();
        Assertions.assertTrue(ownerOptional.isPresent());
        Assertions.assertEquals(ownerOptional.get().getOfflinePlayer(), this.member);
        Mockito.verify(this.owner, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testRemoveOwnerWithAnotherInvite() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.invitePlayer(this.member, owner);

        int initialSize = party.getMembers().size();
        party.removeMember(this.owner, false);

        Assertions.assertEquals(0, party.getInvites().size());
        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(party.getMember(this.owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());
        Assertions.assertTrue(party.getOwner().isEmpty());
        Mockito.verify(this.scheduler).cancelTask(taskId);
    }

    @Test
    public void testDisbandWithMember() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.addMember(this.member);

        int initialSize = party.getMembers().size();
        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        party.disband();

        Assertions.assertEquals(initialSize, playersRemoved[0]);
        Assertions.assertTrue(party.getMembers().isEmpty());
    }

    @Test
    public void testDisbandWithInvite() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.invitePlayer(this.member, this.owner);

        int initialSize = party.getMembers().size();
        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        party.disband();

        Assertions.assertEquals(initialSize, playersRemoved[0]);
        Assertions.assertTrue(party.getMembers().isEmpty());
        Mockito.verify(this.scheduler).cancelTask(taskId);
    }

    @Test
    public void testKickOfflineWithOnlineOwner() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Server server = Mockito.mock(Server.class);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(false);
        Mockito.when(this.member.getServer()).thenReturn(server);
        Mockito.when(server.getOfflinePlayer(this.member.getUniqueId())).thenReturn(this.member);
        party.addMember(this.member);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        int onlineCount = party.getOnlinePlayers().size();
        int offlineCount = party.getMembers().size() - onlineCount;
        party.kickOffline();

        Assertions.assertEquals(onlineCount, party.getOnlinePlayers().size());
        Assertions.assertEquals(offlineCount, playersRemoved[0]);
    }

    @Test // also strange
    public void testKickOfflineWithOfflineOwner() {
        Mockito.when(this.owner.isOnline()).thenReturn(false);
        Mockito.when(this.owner.getServer()).thenReturn(this.server);
        Mockito.when(this.server.getOfflinePlayer(this.owner.getUniqueId())).thenReturn(this.owner);
        Party party = new Party(this.plugin, MiniMessage.get(), new PartyMember(this.owner), new PartySettings(),
                PartyMember::new);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        party.addMember(this.member);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        int onlineCount = party.getOnlinePlayers().size();
        int offlineCount = party.getMembers().size() - onlineCount;
        party.kickOffline();

        Assertions.assertEquals(onlineCount, party.getOnlinePlayers().size());
        Assertions.assertEquals(offlineCount, playersRemoved[0]);

        Optional<PartyMember> owner = party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(this.member, owner.get().getOfflinePlayer());
    }

}
