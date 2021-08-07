package io.github.zap.party.party;

import net.kyori.adventure.text.Component;
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

    private Plugin plugin;

    private Server server;

    private BukkitScheduler scheduler;

    @BeforeEach
    public void setup() {
        this.plugin = Mockito.mock(Plugin.class);
        this.server = Mockito.mock(Server.class);
        this.scheduler = Mockito.mock(BukkitScheduler.class);

        Mockito.when(this.plugin.getServer()).thenReturn(this.server);
        Mockito.when(this.server.getScheduler()).thenReturn(this.scheduler);
    }

    @Test
    public void testBroadcastMessage() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        party.addMember(member);

        Component component = Component.text("Hello, World!");
        party.broadcastMessage(component);

        for (PartyMember partyMember : party.getMembers()) {
            partyMember.getPlayerIfOnline().ifPresent(player ->
                    Mockito.verify(player).sendMessage(ArgumentMatchers.eq(component)));
        }
    }

    @Test
    public void testTransferToPlayerInParty() {
        Player oldOwnerPlayer = Mockito.mock(Player.class);
        Mockito.when(oldOwnerPlayer.getPlayer()).thenReturn(oldOwnerPlayer);
        Mockito.when(oldOwnerPlayer.isOnline()).thenReturn(true);
        Mockito.when(oldOwnerPlayer.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(oldOwnerPlayer.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(oldOwnerPlayer), new PartySettings(), PartyMember::new);

        Player newOwnerPlayer = Mockito.mock(Player.class);
        Mockito.when(newOwnerPlayer.getPlayer()).thenReturn(newOwnerPlayer);
        Mockito.when(newOwnerPlayer.isOnline()).thenReturn(true);
        Mockito.when(newOwnerPlayer.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(newOwnerPlayer.displayName()).thenReturn(Component.text("BigDip123"));

        Optional<PartyMember> newOwner = party.addMember(newOwnerPlayer);
        Assertions.assertTrue(newOwner.isPresent());

        party.transferPartyToPlayer(newOwnerPlayer);

        Optional<PartyMember> owner = party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(owner.get(), newOwner.get());
    }

    @Test
    public void testTransferToPlayerNotInParty() {
        Player oldOwnerPlayer = Mockito.mock(Player.class);
        Mockito.when(oldOwnerPlayer.getPlayer()).thenReturn(oldOwnerPlayer);
        Mockito.when(oldOwnerPlayer.isOnline()).thenReturn(true);
        Mockito.when(oldOwnerPlayer.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(oldOwnerPlayer.displayName()).thenReturn(Component.text("VeryAverage"));
        PartyMember oldOwner = new PartyMember(oldOwnerPlayer);
        Party party = new Party(this.plugin, oldOwner, new PartySettings(), PartyMember::new);

        Player newOwnerPlayer = Mockito.mock(Player.class);
        Mockito.when(newOwnerPlayer.getPlayer()).thenReturn(newOwnerPlayer);
        Mockito.when(newOwnerPlayer.isOnline()).thenReturn(true);
        Mockito.when(newOwnerPlayer.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(newOwnerPlayer.displayName()).thenReturn(Component.text("BigDip123"));
        party.transferPartyToPlayer(newOwnerPlayer);

        Optional<PartyMember> owner = party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(oldOwner, owner.get());
    }

    @Test
    public void testInvitePlayerNotInPartyWithoutExpiration() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        party.invitePlayer(member, owner);
        Assertions.assertEquals(1, party.getInvites().size());

        Collection<PartyMember> initialMembers = party.getMembers();
        party.addMember(member); //sfds test add membser with invite

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
        Mockito.verify(member, Mockito.times(2))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testInvitePlayerNotInPartyWithExpiration() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

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

        Player noob = Mockito.mock(Player.class);
        Mockito.when(noob.getPlayer()).thenReturn(noob);
        Mockito.when(noob.isOnline()).thenReturn(true);
        Mockito.when(noob.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(noob.displayName()).thenReturn(Component.text("BigDip123"));
        party.invitePlayer(noob, owner);
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
        Mockito.verify(noob, Mockito.times(2))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testInviteRecreatedPlayer() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Player noob = Mockito.mock(Player.class);
        Mockito.when(noob.getPlayer()).thenReturn(noob);
        Mockito.when(noob.isOnline()).thenReturn(true);
        Mockito.when(noob.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(noob.displayName()).thenReturn(Component.text("BigDip123"));
        party.invitePlayer(noob, owner);

        OfflinePlayer reborn = Mockito.mock(OfflinePlayer.class);
        Mockito.when(reborn.getPlayer()).thenReturn(null);
        Mockito.when(reborn.isOnline()).thenReturn(false);
        Mockito.when(reborn.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));

        Assertions.assertTrue(party.hasInvite(reborn));
    }

    @Test
    public void testAddPlayerNotInParty() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

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

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));

        int initialSize = party.getMembers().size();
        party.invitePlayer(member, owner);
        Optional<PartyMember> newMember = party.addMember(member);
        Assertions.assertEquals(0, party.getInvites().size());

        Assertions.assertEquals(1, playersAdded[0]);
        Assertions.assertEquals(initialSize + 1, party.getMembers().size());
        Assertions.assertTrue(newMember.isPresent());
    }

    @Test
    public void testAddPlayerInParty() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        Player noob = Mockito.mock(Player.class);
        Mockito.when(noob.getPlayer()).thenReturn(noob);
        Mockito.when(noob.isOnline()).thenReturn(true);
        Mockito.when(noob.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(noob.displayName()).thenReturn(Component.text("BigDip123"));
        party.addMember(noob);

        int initialSize = party.getMembers().size();
        party.registerJoinHandler(player -> Assertions.fail("No player join handler should be called."));
        Optional<PartyMember> newMember = party.addMember(noob);

        Assertions.assertEquals(initialSize, party.getMembers().size());
        Assertions.assertTrue(newMember.isEmpty());
    }

    @Test
    public void testRemovePlayerNotInParty() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        party.registerLeaveHandler(player -> Assertions.fail("No player leave handler should be called."));

        Player noob = Mockito.mock(Player.class);
        Mockito.when(noob.getPlayer()).thenReturn(noob);
        Mockito.when(noob.isOnline()).thenReturn(true);
        Mockito.when(noob.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(noob.displayName()).thenReturn(Component.text("BigDip123"));

        int initialSize = party.getMembers().size();
        party.removeMember(noob, false);

        Assertions.assertEquals(initialSize, party.getMembers().size());
    }

    @Test
    public void testRemovePlayerInParty() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(member).sendMessage(ArgumentMatchers.any(Component.class));
        party.addMember(member);

        freeze[0] = true;
        int initialSize = party.getMembers().size();
        party.removeMember(member, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());
        Assertions.assertTrue(party.getMember(member).isEmpty());
        Mockito.verify(member, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test // a bit strange but meh
    public void testRemoveOwnerWithAnotherMember() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        int[] playersRemoved = new int[] { 0 };
        party.registerLeaveHandler(player -> {
            Assertions.assertFalse(party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(owner).sendMessage(ArgumentMatchers.any(Component.class));
        party.addMember(member);

        freeze[0] = true;
        int initialSize = party.getMembers().size();
        party.removeMember(owner, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(party.getMember(owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());

        Optional<PartyMember> ownerOptional = party.getOwner();
        Assertions.assertTrue(ownerOptional.isPresent());
        Assertions.assertEquals(ownerOptional.get().getOfflinePlayer(), member);
        Mockito.verify(owner, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testRemoveOwnerWithAnotherInvite() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

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

        Player noob = Mockito.mock(Player.class);
        Mockito.when(noob.getPlayer()).thenReturn(noob);
        Mockito.when(noob.isOnline()).thenReturn(true);
        Mockito.when(noob.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(noob.displayName()).thenReturn(Component.text("BigDip123"));
        party.invitePlayer(noob, owner);

        int initialSize = party.getMembers().size();
        party.removeMember(owner, false);

        Assertions.assertEquals(0, party.getInvites().size());
        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(party.getMember(owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, party.getMembers().size());
        Assertions.assertTrue(party.getOwner().isEmpty());
        Mockito.verify(this.scheduler).cancelTask(taskId);
    }

    @Test
    public void testDisbandWithMember() {
        Player ownerPlayer = Mockito.mock(Player.class);
        Mockito.when(ownerPlayer.getPlayer()).thenReturn(ownerPlayer);
        Mockito.when(ownerPlayer.isOnline()).thenReturn(true);
        Mockito.when(ownerPlayer.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(ownerPlayer.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(ownerPlayer), new PartySettings(), PartyMember::new);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        party.addMember(member);

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
        Player ownerPlayer = Mockito.mock(Player.class);
        Mockito.when(ownerPlayer.getPlayer()).thenReturn(ownerPlayer);
        Mockito.when(ownerPlayer.isOnline()).thenReturn(true);
        Mockito.when(ownerPlayer.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(ownerPlayer.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(ownerPlayer), new PartySettings(), PartyMember::new);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        party.invitePlayer(member, ownerPlayer);

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
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getPlayer()).thenReturn(owner);
        Mockito.when(owner.isOnline()).thenReturn(true);
        Mockito.when(owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Party party = new Party(this.plugin, new PartyMember(owner), new PartySettings(), PartyMember::new);

        Server server = Mockito.mock(Server.class);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(false);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        Mockito.when(member.getServer()).thenReturn(server);
        Mockito.when(server.getOfflinePlayer(member.getUniqueId())).thenReturn(member);
        party.addMember(member);

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
        Player ownerPlayer = Mockito.mock(Player.class);
        Mockito.when(ownerPlayer.isOnline()).thenReturn(false);
        Mockito.when(ownerPlayer.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(ownerPlayer.displayName()).thenReturn(Component.text("VeryAverage"));
        Mockito.when(ownerPlayer.getServer()).thenReturn(this.server);
        Mockito.when(this.server.getOfflinePlayer(ownerPlayer.getUniqueId())).thenReturn(ownerPlayer);
        Party party = new Party(this.plugin, new PartyMember(ownerPlayer), new PartySettings(), PartyMember::new);

        Player member = Mockito.mock(Player.class);
        Mockito.when(member.getPlayer()).thenReturn(member);
        Mockito.when(member.isOnline()).thenReturn(true);
        Mockito.when(member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(member.displayName()).thenReturn(Component.text("BigDip123"));
        party.addMember(member);

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
        Assertions.assertEquals(member, owner.get().getOfflinePlayer());
    }

}
