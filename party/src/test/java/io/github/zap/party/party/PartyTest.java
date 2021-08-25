package io.github.zap.party.party;

import io.github.zap.party.party.chat.BasicPartyChatHandler;
import io.github.zap.party.party.invitation.TimedInvitationManager;
import io.github.zap.party.party.list.BasicPartyLister;
import io.github.zap.party.party.list.PartyLister;
import io.github.zap.party.party.member.PartyMember;
import io.github.zap.party.party.namer.OfflinePlayerNamer;
import io.github.zap.party.party.namer.SingleTextColorOfflinePlayerNamer;
import io.github.zap.party.party.settings.PartySettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.Random;
import java.util.UUID;

public class PartyTest {

    private final static int BEST_TICK = 69;

    private final UUID veryAverageUUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    private final UUID bigDipUUID = UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692");

    private final MiniMessage miniMessage = MiniMessage.get();

    private Plugin plugin;

    private Server server;

    private BukkitScheduler scheduler;

    private Party party;

    private Player owner;

    private Player member;

    @BeforeEach
    public void setup() {
        this.plugin = Mockito.mock(Plugin.class);
        this.scheduler = Mockito.mock(BukkitScheduler.class);

        this.server = Mockito.mock(Server.class);
        Mockito.when(this.server.getCurrentTick()).thenReturn(BEST_TICK);

        Mockito.when(this.plugin.getServer()).thenReturn(this.server);
        Mockito.when(this.server.getScheduler()).thenReturn(this.scheduler);

        this.owner = Mockito.mock(Player.class);
        Mockito.when(this.owner.getUniqueId()).thenReturn(this.veryAverageUUID);
        Mockito.when(this.owner.displayName()).thenReturn(Component.text("VeryAverage"));
        Mockito.when(this.owner.getServer()).thenReturn(this.server);

        this.member = Mockito.mock(Player.class);
        Mockito.when(this.member.getUniqueId()).thenReturn(this.bigDipUUID);
        Mockito.when(this.member.displayName()).thenReturn(Component.text("BigDip123"));
        Mockito.when(this.member.getServer()).thenReturn(this.server);

        Mockito.when(this.server.getOfflinePlayer(this.owner.getUniqueId())).thenReturn(this.owner);
        Mockito.when(this.server.getOfflinePlayer(this.member.getUniqueId())).thenReturn(this.member);

        this.plugin = Mockito.mock(Plugin.class);
        Mockito.when(this.plugin.getServer()).thenReturn(this.server);

        Random random = new Random();
        OfflinePlayerNamer playerNamer = new SingleTextColorOfflinePlayerNamer();
        PartyLister partyLister = new BasicPartyLister(this.plugin, this.miniMessage,
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.GREEN),
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.RED),
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.BLUE));
        this.party = new Party(this.miniMessage, random, new PartyMember(this.owner),
                new PartySettings(), PartyMember::new,
                new TimedInvitationManager(this.plugin, this.miniMessage, playerNamer),
                new BasicPartyChatHandler(this.plugin, this.miniMessage), partyLister, playerNamer);
    }

    @Test
    public void testBroadcastMessage() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.addMember(this.member);

        Component component = Component.text("Hello, World!");
        this.party.broadcastMessage(component);

        for (PartyMember partyMember : this.party.getMembers()) {
            partyMember.getPlayerIfOnline()
                    .ifPresent(player -> Mockito.verify(player).sendMessage(ArgumentMatchers.eq(component)));
        }
    }

    @Test
    public void testTransferToPlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        Optional<PartyMember> newOwner = this.party.addMember(this.member);
        Assertions.assertTrue(newOwner.isPresent());

        this.party.transferPartyToPlayer(this.member);

        Optional<PartyMember> owner = this.party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(owner.get(), newOwner.get());
    }

    @Test
    public void testTransferToPlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Optional<PartyMember> oldOwnerOptional = this.party.getOwner();
        Assertions.assertTrue(oldOwnerOptional.isPresent());
        PartyMember oldOwner = oldOwnerOptional.get();

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            this.party.transferPartyToPlayer(this.member);
        });

        Optional<PartyMember> owner = this.party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(oldOwner, owner.get());
    }

    @Test
    public void testInvitePlayerNotInPartyWithoutExpiration() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);
        Assertions.assertEquals(1, this.party.getInvitationManager().getInvitations().size());

        Collection<PartyMember> initialMembers = this.party.getMembers();
        this.party.addMember(this.member);

        Mockito.verify(this.scheduler).runTaskLater(ArgumentMatchers.eq(this.plugin),
                ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()));
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

        Runnable[] runnable = new Runnable[1];
        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime())))
                .then((Answer<BukkitTask>) invocation -> {
                    runnable[0] = invocation.getArgument(1);
                    return bukkitTask;
                });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);
        Assertions.assertEquals(1, this.party.getInvitationManager().getInvitations().size());
        runnable[0].run();
        Assertions.assertEquals(0, this.party.getInvitationManager().getInvitations().size());

        Collection<PartyMember> initialMembers = this.party.getMembers();

        Mockito.verify(this.scheduler).runTaskLater(ArgumentMatchers.eq(this.plugin),
                ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()));
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

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);

        OfflinePlayer reborn = Mockito.mock(OfflinePlayer.class);
        Mockito.when(reborn.getPlayer()).thenReturn(null);
        Mockito.when(reborn.isOnline()).thenReturn(false);
        Mockito.when(reborn.getUniqueId()).thenReturn(this.bigDipUUID);

        Assertions.assertTrue(this.party.getInvitationManager().hasInvitation(reborn));
    }

    @Test
    public void testAddPlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        int[] playersAdded = new int[] { 0 };
        this.party.registerJoinHandler(player -> {
            Assertions.assertTrue(this.party.hasMember(player.getOfflinePlayer()));
            playersAdded[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        int initialSize = this.party.getMembers().size();
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);
        Optional<PartyMember> newMember = this.party.addMember(this.member);
        Assertions.assertEquals(0, this.party.getInvitationManager().getInvitations().size());

        Assertions.assertEquals(1, playersAdded[0]);
        Assertions.assertEquals(initialSize + 1, this.party.getMembers().size());
        Assertions.assertTrue(newMember.isPresent());
    }

    @Test
    public void testAddPlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.addMember(this.member);

        int initialSize = this.party.getMembers().size();
        this.party.registerJoinHandler(player -> Assertions.fail("No player join handler should be called."));
        Optional<PartyMember> newMember = this.party.addMember(this.member);

        Assertions.assertEquals(initialSize, this.party.getMembers().size());
        Assertions.assertTrue(newMember.isEmpty());
    }

    @Test
    public void testRemovePlayerNotInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        this.party.registerLeaveHandler(player -> Assertions.fail("No player leave handler should be called."));

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);

        int initialSize = this.party.getMembers().size();
        this.party.removeMember(this.member, false);

        Assertions.assertEquals(initialSize, this.party.getMembers().size());
    }

    @Test
    public void testRemovePlayerInParty() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
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
        this.party.addMember(this.member);

        freeze[0] = true;
        int initialSize = this.party.getMembers().size();
        this.party.removeMember(this.member, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertEquals(initialSize - 1, this.party.getMembers().size());
        Assertions.assertTrue(this.party.getMember(this.member).isEmpty());
        Mockito.verify(this.member, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test // a bit strange but meh
    public void testRemoveOwnerWithAnotherMember() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
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
        this.party.addMember(this.member);

        freeze[0] = true;
        int initialSize = this.party.getMembers().size();
        this.party.removeMember(this.owner, false);

        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(this.party.getMember(this.owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, this.party.getMembers().size());

        Optional<PartyMember> ownerOptional = this.party.getOwner();
        Assertions.assertTrue(ownerOptional.isPresent());
        Assertions.assertEquals(ownerOptional.get().getOfflinePlayer(), this.member);
        Mockito.verify(this.owner, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testRemoveOwnerWithAnotherInvite() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);

        int initialSize = this.party.getMembers().size();
        this.party.removeMember(this.owner, false);

        Assertions.assertEquals(0, this.party.getInvitationManager().getInvitations().size());
        Assertions.assertEquals(1, playersRemoved[0]);
        Assertions.assertTrue(this.party.getMember(this.owner).isEmpty());
        Assertions.assertEquals(initialSize - 1, this.party.getMembers().size());
        Assertions.assertTrue(this.party.getOwner().isEmpty());
        Mockito.verify(this.scheduler).cancelTask(taskId);
    }

    @Test
    public void testDisbandWithMember() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.addMember(this.member);

        int initialSize = this.party.getMembers().size();
        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        this.party.disband();

        Assertions.assertEquals(initialSize, playersRemoved[0]);
        Assertions.assertTrue(this.party.getMembers().isEmpty());
    }

    @Test
    public void testDisbandWithInvite() {
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        int taskId = 69;
        BukkitTask bukkitTask = Mockito.mock(BukkitTask.class);
        Mockito.when(bukkitTask.getTaskId()).thenReturn(taskId);
        Mockito.when(this.scheduler.runTaskLater(ArgumentMatchers.eq(this.plugin), ArgumentMatchers.any(Runnable.class),
                ArgumentMatchers.eq(this.party.getPartySettings().getInviteExpirationTime()))).thenReturn(bukkitTask);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.getInvitationManager().addInvitation(this.party, this.member, this.owner);

        int initialSize = this.party.getMembers().size();
        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        this.party.disband();

        Assertions.assertEquals(initialSize, playersRemoved[0]);
        Assertions.assertTrue(this.party.getMembers().isEmpty());
        Mockito.verify(this.scheduler).cancelTask(taskId);
    }

    @Test
    public void testKickOfflineWithOnlineOwner() {
        Mockito.when(this.server.getPlayer(this.owner.getUniqueId())).thenReturn(this.owner);

        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(false);
        this.party.addMember(this.member);

        Mockito.when(this.server.getCurrentTick()).thenReturn(BEST_TICK + 1);

        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        int onlineCount = this.party.getOnlinePlayers().size();
        int offlineCount = this.party.getMembers().size() - onlineCount;
        this.party.kickOffline();

        Assertions.assertEquals(onlineCount, this.party.getOnlinePlayers().size());
        Assertions.assertEquals(offlineCount, playersRemoved[0]);
    }

    @Test // also strange
    public void testKickOfflineWithOfflineOwner() {
        Mockito.when(this.server.getPlayer(this.member.getUniqueId())).thenReturn(this.member);

        Mockito.when(this.owner.isOnline()).thenReturn(false);

        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        this.party.addMember(this.member);

        Mockito.when(this.server.getCurrentTick()).thenReturn(BEST_TICK + 1);

        int[] playersRemoved = new int[] { 0 };
        this.party.registerLeaveHandler(player -> {
            Assertions.assertFalse(this.party.hasMember(player.getOfflinePlayer()));
            playersRemoved[0]++;
        });

        int onlineCount = this.party.getOnlinePlayers().size();
        int offlineCount = this.party.getMembers().size() - onlineCount;
        this.party.kickOffline();

        Assertions.assertEquals(onlineCount, this.party.getOnlinePlayers().size());
        Assertions.assertEquals(offlineCount, playersRemoved[0]);

        Optional<PartyMember> owner = this.party.getOwner();
        Assertions.assertTrue(owner.isPresent());
        Assertions.assertEquals(this.member, owner.get().getOfflinePlayer());
    }

}
