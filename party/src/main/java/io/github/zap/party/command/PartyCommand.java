package io.github.zap.party.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.party.party.creator.PartyCreator;
import io.github.zap.party.plugin.tracker.PartyTracker;
import org.jetbrains.annotations.NotNull;

/**
 * Generic party command.
 */
public class PartyCommand extends RegularCommand {

    public PartyCommand(@NotNull PartyTracker partyTracker, @NotNull PartyCreator partyCreator) {
        super("party");
        addForm(new PartySettingsForm(partyTracker));
        addForm(new PartyChatForm(partyTracker));
        addForm(new CreatePartyForm(partyTracker, partyCreator));
        addForm(new InvitePlayerForm(partyTracker, partyCreator));
        addForm(new JoinPartyForm(partyTracker));
        addForm(new LeavePartyForm(partyTracker));
        addForm(new ListMembersForm(partyTracker));
        addForm(new PartyMuteForm(partyTracker));
        addForm(new KickMemberForm(partyTracker));
        addForm(new KickOfflineMembersForm(partyTracker));
        addForm(new TransferPartyForm(partyTracker));
        addForm(new DisbandPartyForm(partyTracker));
    }

}
