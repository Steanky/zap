package io.github.zap.party.party.creator;

import io.github.zap.party.party.Party;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * {@link java.util.function.Function}-like creator for {@link Party}s.
 */
public interface PartyCreator {

    /**
     * Creates a new {@link Party}.
     * @param owner The initial owner of the party
     * @return The new {@link Party}
     */
    @NotNull Party createParty(@NotNull Player owner);

}
