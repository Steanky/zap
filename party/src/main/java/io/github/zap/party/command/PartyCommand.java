package io.github.zap.party.command;

import io.github.regularcommands.commands.RegularCommand;

public class PartyCommand extends RegularCommand {

    public PartyCommand() {
        super("party");
        addForm(new InvitePlayerForm());
        addForm(new JoinPartyForm());
    }

}
