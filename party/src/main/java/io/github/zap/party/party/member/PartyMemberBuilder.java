package io.github.zap.party.party.member;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Creates {@link PartyMember}s.
 */
@FunctionalInterface
public interface PartyMemberBuilder {

    /**
     * Creates a {@link PartyMember}.
     * @param player The {@link Player} to create the {@link PartyMember} for
     * @return The new {@link PartyMember}
     */
    @NotNull PartyMember createPartyMember(@NotNull Player player);

}
