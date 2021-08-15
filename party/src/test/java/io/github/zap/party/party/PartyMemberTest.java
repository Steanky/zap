package io.github.zap.party.party;

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

    private Player veryAverage;

    @BeforeEach
    public void setup() {
        this.veryAverage = Mockito.mock(Player.class);
        Mockito.when(this.veryAverage.getUniqueId())
                .thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(this.veryAverage.displayName()).thenReturn(Component.text("VeryAverage"));
    }

    @Test
    public void testGetPlayerIfOnlineWhenOnline() {
        Mockito.when(this.veryAverage.isOnline()).thenReturn(true);
        Mockito.when(this.veryAverage.getPlayer()).thenReturn(this.veryAverage);

        PartyMember partyMember = new PartyMember(this.veryAverage);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isPresent());
        Assertions.assertEquals(this.veryAverage, playerOptional.get());
    }

    @Test
    public void testGetPlayerIfOnlineWhenNotOnline() {
        Server server = Mockito.mock(Server.class);

        Mockito.when(this.veryAverage.isOnline()).thenReturn(false);
        Mockito.when(this.veryAverage.getServer()).thenReturn(server);
        Mockito.when(server.getPlayer(this.veryAverage.getUniqueId())).thenReturn(this.veryAverage);

        PartyMember partyMember = new PartyMember(this.veryAverage);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isEmpty());
    }

}
