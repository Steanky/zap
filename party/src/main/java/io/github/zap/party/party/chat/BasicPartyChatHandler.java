package io.github.zap.party.party.chat;

import io.github.zap.party.party.Party;
import io.github.zap.party.party.member.PartyMember;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

/**
 * Basic implementation of a {@link PartyChatHandler}.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicPartyChatHandler implements PartyChatHandler {

    private final Plugin plugin;

    private final MiniMessage miniMessage;

    /**
     * Creates a simple party chat handler that deals with parties being muted and party chat.
     * @param plugin The plugin this chat handler belongs to
     * @param miniMessage A {@link MiniMessage} instance to parse messages
     */
    public BasicPartyChatHandler(@NotNull Plugin plugin, @NotNull MiniMessage miniMessage) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
    }

    @Override
    public void onAsyncChat(@NotNull Party party, AsyncChatEvent event) {
        Optional<PartyMember> optionalPartyMember = party.getMember(event.getPlayer());
        if (optionalPartyMember.isEmpty()) {
            return;
        }

        PartyMember partyMember = optionalPartyMember.get();
        if (!partyMember.isInPartyChat()) {
            return;
        }

        if (partyMember.isMuted()) {
            event.getPlayer().sendMessage(this.miniMessage.parse("<red>You are muted from speaking " +
                    "in the party chat."));
            event.setCancelled(true);
        }
        else if (party.getPartySettings().isMuted() && !party.isOwner(event.getPlayer())) {
            event.getPlayer().sendMessage(this.miniMessage.parse("<red>The party chat is muted."));
            event.setCancelled(true);
        }
        else {
            Iterator<Audience> iterator = event.viewers().iterator();
            while (iterator.hasNext()) {
                Audience audience = iterator.next();
                if (!(audience instanceof Player player && party.hasMember(player))) {
                    try {
                        iterator.remove();
                    }
                    catch (UnsupportedOperationException e) {
                        this.plugin.getLogger().info("Could not prevent sending a party chat message to " +
                                audience + " from " + event.getPlayer().getName() + " due to an event being called " +
                                "which does not support audience removal!");
                    }
                }
            }

            ChatRenderer oldRenderer = event.renderer();
            event.renderer((source, sourceDisplayName, message, viewer) -> {
                Component render = oldRenderer.render(source, sourceDisplayName, message, viewer);
                return this.miniMessage.parse("<blue>Party <dark_gray>> <reset><message>",
                        Template.of("message", render));
            });
        }
    }

}
