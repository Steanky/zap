package io.github.zap.party.party;

import io.github.zap.party.party.member.PartyMember;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

public class PartyMemberTest {

    private final static int BEST_TICK = 69;

    private Server server;

    private Player member;

    @BeforeEach
    public void setup() {
        this.server = Mockito.mock(Server.class);
        Mockito.when(this.server.getCurrentTick()).thenReturn(BEST_TICK).thenReturn(BEST_TICK + 1);

        this.member = Mockito.mock(Player.class);
        Mockito.when(this.member.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(this.member.displayName()).thenReturn(Component.text("VeryAverage"));

        Mockito.when(this.member.getServer()).thenReturn(this.server);
    }

    @Test
    public void testGetPlayerIfOnlineWhenOnline() {
        Mockito.when(this.member.isOnline()).thenReturn(true);
        Mockito.when(this.member.getPlayer()).thenReturn(this.member);
        Mockito.when(this.server.getPlayer(this.member.getUniqueId())).thenReturn(this.member);

        PartyMember partyMember = new PartyMember(this.member);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isPresent());
        Assertions.assertEquals(this.member, playerOptional.get());
    }

    @Test
    public void testGetPlayerIfOnlineWhenNotOnline() {
        Mockito.when(this.member.isOnline()).thenReturn(false);
        Mockito.when(this.server.getPlayer(this.member.getUniqueId())).thenReturn(null);

        PartyMember partyMember = new PartyMember(this.member);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isEmpty());
    }

}
