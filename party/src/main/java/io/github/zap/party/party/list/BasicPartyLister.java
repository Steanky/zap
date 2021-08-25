package io.github.zap.party.party.list;

import io.github.zap.party.party.Party;
import io.github.zap.party.party.member.PartyMember;
import io.github.zap.party.party.namer.OfflinePlayerNamer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Basic implementation of a {@link PartyLister}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicPartyLister implements PartyLister {

    private final Plugin plugin;

    private final MiniMessage miniMessage;

    private final OfflinePlayerNamer onlineMemberNamer;

    private final OfflinePlayerNamer offlineMemberNamer;

    private final OfflinePlayerNamer invitedNamer;

    /**
     * Creates a simple party lister.
     * @param plugin The plugin that this party lister belongs to
     * @param miniMessage A {@link MiniMessage} instance to parse messages
     * @param onlineMemberNamer A namer for online members
     * @param offlineMemberNamer A namer for offline members
     * @param invitedNamer A namer for invited players
     */
    public BasicPartyLister(@NotNull Plugin plugin, @NotNull MiniMessage miniMessage,
                            @NotNull OfflinePlayerNamer onlineMemberNamer,
                            @NotNull OfflinePlayerNamer offlineMemberNamer, @NotNull OfflinePlayerNamer invitedNamer) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
        this.onlineMemberNamer = onlineMemberNamer;
        this.offlineMemberNamer = offlineMemberNamer;
        this.invitedNamer = invitedNamer;
    }

    @Override
    public @NotNull Collection<Component> getPartyListComponents(@NotNull Party party) {
        TextComponent.Builder online = Component.text().append(this.miniMessage.parse("<green>Online<white>: "));
        TextComponent.Builder offline = Component.text().append(this.miniMessage.parse("<red>Offline<white>: "));
        TextComponent.Builder invited = Component.text().append(this.miniMessage.parse("<blue>Invites<white>: "));

        Collection<PartyMember> memberCollection = party.getMembers();
        List<Player> onlinePlayers = new ArrayList<>(memberCollection.size());
        List<OfflinePlayer> offlinePlayers = new ArrayList<>(memberCollection.size());

        for (PartyMember member : memberCollection) {
            OfflinePlayer offlinePlayer = member.getOfflinePlayer();
            Player onlinePlayer = offlinePlayer.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayers.add(onlinePlayer);
            } else {
                offlinePlayers.add(offlinePlayer);
            }
        }

        Component comma = this.miniMessage.parse("<white>, ");
        for (int i = 0; i < onlinePlayers.size(); i++) {
            online.append(this.onlineMemberNamer.name(onlinePlayers.get(i)));

            if (i < onlinePlayers.size() - 1) {
                online.append(comma);
            }
        }

        for (int i = 0; i < offlinePlayers.size(); i++) {
            online.append(this.offlineMemberNamer.name(offlinePlayers.get(i)));
            if (i < offlinePlayers.size() - 1) {
                offline.append(comma);
            }
        }

        Iterator<UUID> iterator = party.getInvitationManager().getInvitations().iterator();
        while (iterator.hasNext()) {
            invited.append(this.invitedNamer.name(this.plugin.getServer().getOfflinePlayer(iterator.next())));
            if (iterator.hasNext()) {
                invited.append(comma);
            }
        }

        return List.of(online.build(), offline.build(), invited.build());
    }

}
