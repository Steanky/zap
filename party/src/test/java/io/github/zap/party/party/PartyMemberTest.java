package io.github.zap.party.party;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

public class PartyMemberTest {

    @Test
    public void testGetPlayerIfOnlineWhenOnline() {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.isOnline()).thenReturn(true);
        Mockito.when(player.getPlayer()).thenReturn(player);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));

        PartyMember partyMember = new PartyMember(player);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isPresent());
        Assertions.assertEquals(player, playerOptional.get());
    }

    @Test
    public void testGetPlayerIfOnlineWhenNotOnline() {
        Server server = Mockito.mock(Server.class);

        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127"));
        Mockito.when(player.displayName()).thenReturn(Component.text("VeryAverage"));
        Mockito.when(player.isOnline()).thenReturn(false);
        Mockito.when(player.getServer()).thenReturn(server);
        Mockito.when(server.getPlayer(player.getUniqueId())).thenReturn(player);

        PartyMember partyMember = new PartyMember(player);
        Optional<Player> playerOptional = partyMember.getPlayerIfOnline();
        Assertions.assertTrue(playerOptional.isEmpty());
    }

}
