package io.github.zap.party.command;

import io.github.regularcommands.commands.RegularCommand;

/**
 * Generic party command
 */
public class PartyCommand extends RegularCommand {

    public PartyCommand() {
        super("party");
        addForm(new InvitePlayerForm());
        addForm(new JoinPartyForm());
        addForm(new LeavePartyForm());
        addForm(new ListMembersForm());
        addForm(new DisbandPartyForm());
    }

}
