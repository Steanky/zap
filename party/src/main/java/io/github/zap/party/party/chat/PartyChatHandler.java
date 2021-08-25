package io.github.zap.party.party.chat;

import io.github.zap.party.party.Party;
import io.github.zap.party.party.member.PartyMember;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Handles chat from {@link PartyMember}s.
 */
@FunctionalInterface
public interface PartyChatHandler extends Listener {

    /**
     * Called when a player chats.
     * The player does not necessarily come from the specified party and must be validated.
     * @param party The party to check against
     * @param event The chat event
     */
    void onAsyncChat(@NotNull Party party, AsyncChatEvent event);

}
