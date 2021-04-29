package io.github.zap.party.command;

import io.github.regularcommands.commands.RegularCommand;

/**
 * Generic party command
 */
public class PartyCommand extends RegularCommand {

    public PartyCommand() {
        super("party");
        addForm(new PartyChatForm());
        addForm(new CreatePartyForm());
        addForm(new InvitePlayerForm());
        addForm(new JoinPartyForm());
        addForm(new LeavePartyForm());
        addForm(new ListMembersForm());
        addForm(new KickMemberForm());
        addForm(new KickOfflineMembersForm());
        addForm(new TransferPartyForm());
        addForm(new DisbandPartyForm());
    }

}
