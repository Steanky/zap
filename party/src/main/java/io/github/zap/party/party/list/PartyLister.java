package io.github.zap.party.party.list;

import io.github.zap.party.party.Party;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Gets a list of {@link Component}s for a party.
 * This is used in the {@link io.github.zap.party.command.ListMembersForm}.
 */
@FunctionalInterface
public interface PartyLister {

    /**
     * Gets a collection of {@link Component}s for display.
     * @param party The party to get {@link Component}s for.
     * @return A collection of the display {@link Component}s
     */
    @NotNull Collection<Component> getPartyListComponents(@NotNull Party party);

}
