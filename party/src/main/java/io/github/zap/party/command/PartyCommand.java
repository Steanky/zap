package io.github.zap.party.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.party.PartyPlusPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Generic party command
 */
public class PartyCommand extends RegularCommand {

    public PartyCommand(@NotNull PartyPlusPlus partyPlusPlus, @NotNull MiniMessage miniMessage) {
        super("party");
        addForm(new PartySettingsForm(partyPlusPlus));
        addForm(new PartyChatForm(partyPlusPlus));
        addForm(new CreatePartyForm(partyPlusPlus));
        addForm(new InvitePlayerForm(partyPlusPlus, miniMessage));
        addForm(new JoinPartyForm(partyPlusPlus));
        addForm(new LeavePartyForm(partyPlusPlus));
        addForm(new ListMembersForm(partyPlusPlus));
        addForm(new PartyMuteForm(partyPlusPlus));
        addForm(new KickMemberForm(partyPlusPlus));
        addForm(new KickOfflineMembersForm(partyPlusPlus));
        addForm(new TransferPartyForm(partyPlusPlus));
        addForm(new DisbandPartyForm(partyPlusPlus));
    }

}
