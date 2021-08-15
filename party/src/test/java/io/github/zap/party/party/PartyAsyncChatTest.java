package io.github.zap.party.party;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import java.util.*;
import java.util.logging.Logger;

public class PartyAsyncChatTest {

    private Player owner, member, noob;

    private Plugin plugin;

    @BeforeAll
    public static void start() {
        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getLogger()).thenReturn(Logger.getLogger("Minecraft"));
        Bukkit.setServer(server); // ._.
    }

    @BeforeEach
    public void setup() {
        this.owner = Mockito.mock(Player.class);
        Mockito.when(this.owner.getPlayer()).thenReturn(this.owner);
        Mockito.when(this.owner.isOnline()).thenReturn(true);
        Mockito.when(this.owner.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(this.owner.displayName()).thenReturn(Component.text("VeryAverage"));
        this.member = Mockito.mock(Player.class);
        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.member.isOnline()).thenReturn(true);
        Mockito.when(this.member.getUniqueId()).thenReturn(UUID.fromString("a7db1c97-6064-46a1-91c6-77a4c974b692"));
        Mockito.when(this.member.displayName()).thenReturn(Component.text("BigDip123"));
        this.noob = Mockito.mock(Player.class);
        Mockito.when(this.noob.getPlayer()).thenReturn(this.noob);
        Mockito.when(this.noob.isOnline()).thenReturn(true);
        Mockito.when(this.noob.getUniqueId()).thenReturn(UUID.fromString("31ee3877-dbd8-423a-95e4-9181b8acfe74"));
        Mockito.when(this.noob.displayName()).thenReturn(Component.text("SimpleCactus"));
        OngoingStubbing<Collection<? extends Player>> ongoingStubbing = Mockito.when(Bukkit.getServer().getOnlinePlayers());
        ongoingStubbing.thenReturn(List.of(this.owner, this.member, this.noob)); // why does java hate me? why can't I put it on one line?
        this.plugin = Mockito.mock(Plugin.class);
    }

    @Test
    public void testAsyncChatFromPlayerNotInParty() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);

        Set<Audience> audiences = Set.of(this.owner, this.noob);
        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.noob, new HashSet<>(audiences),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertFalse(event.isCancelled());
        Assertions.assertEquals(audiences, event.viewers());
        Assertions.assertEquals(event.originalMessage(), event.message());
    }

    @Test
    public void testAsyncChatFromUnmutedPlayerInUnmutedPartyNotInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);
        party.addMember(this.member);

        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertFalse(event.isCancelled());
        Assertions.assertEquals(Set.of(this.owner, this.member, this.noob), event.viewers());
        Assertions.assertEquals(event.originalMessage(), event.message());
    }

    @Test
    public void testAsyncChatFromUnmutedPlayerInUnmutedPartyInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);
        Optional<PartyMember> partyMemberOptional = party.addMember(this.member);

        Assertions.assertTrue(partyMemberOptional.isPresent());
        partyMemberOptional.get().setInPartyChat(true);

        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                ChatRenderer.defaultRenderer(), originalMessage, originalMessage);
        ChatRenderer originalRenderer = event.renderer();
        party.onAsyncChat(event);

        Assertions.assertFalse(event.isCancelled());
        Assertions.assertEquals(Set.of(this.owner, this.member), event.viewers());
        for (Audience audience : event.viewers()) {
            Component oldMessage = originalRenderer.render(this.member, this.member.displayName(),
                    event.originalMessage(), audience);
            Component newMessage = event.renderer().render(this.member, this.member.displayName(),
                    event.message(), audience);
            Assertions.assertTrue(newMessage.children().contains(oldMessage));
        }}

    @Test
    public void testAsyncChatFromMutedPlayerInPartyNotInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);
        Optional<PartyMember> partyMemberOptional = party.addMember(this.member);

        Assertions.assertTrue(partyMemberOptional.isPresent());
        partyMemberOptional.get().setMuted(true);

        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertFalse(event.isCancelled());
        Assertions.assertEquals(Set.of(this.owner, this.member, this.noob), event.viewers());
    }

    @Test
    public void testAsyncChatFromMutedPlayerInPartyInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);

        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(member).sendMessage(ArgumentMatchers.any(Component.class));
        Optional<PartyMember> partyMemberOptional = party.addMember(this.member);

        Assertions.assertTrue(partyMemberOptional.isPresent());
        PartyMember partyMember = partyMemberOptional.get();
        partyMember.setInPartyChat(true);
        partyMember.setMuted(true);

        freeze[0] = true;
        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertTrue(event.isCancelled());
        Mockito.verify(this.member, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

    @Test
    public void testAsyncChatFromPlayerInMutedPartyNotInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);
        party.getPartySettings().setMuted(true);
        party.addMember(this.member);

        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertFalse(event.isCancelled());
        Assertions.assertEquals(Set.of(this.owner, this.member, this.noob), event.viewers());
    }

    @Test
    public void testAsyncChatFromPlayerInMutedPartyInPartyChat() {
        Party party = new Party(this.plugin, new PartyMember(this.owner), new PartySettings(), PartyMember::new);
        party.getPartySettings().setMuted(true);

        boolean[] freeze = new boolean[]{ false };
        int[] counts = new int[]{ 0 };
        Mockito.doAnswer((Answer<Void>) invocation -> {
            if (!freeze[0]) {
                counts[0]++;
            }
            return null;
        }).when(member).sendMessage(ArgumentMatchers.any(Component.class));
        Optional<PartyMember> partyMemberOptional = party.addMember(this.member);

        Assertions.assertTrue(partyMemberOptional.isPresent());
        partyMemberOptional.get().setInPartyChat(true);

        freeze[0] = true;
        Component originalMessage = Component.text("Hello, World!");
        AsyncChatEvent event = new AsyncChatEvent(true, this.member,
                new HashSet<>(Set.of(this.owner, this.member, this.noob)),
                Mockito.mock(ChatRenderer.class), originalMessage, originalMessage);
        party.onAsyncChat(event);

        Assertions.assertTrue(event.isCancelled());
        Mockito.verify(this.member, Mockito.times(counts[0] + 1))
                .sendMessage(ArgumentMatchers.any(Component.class));
    }

}
