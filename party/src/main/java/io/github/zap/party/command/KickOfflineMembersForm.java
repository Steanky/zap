package io.github.zap.party.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.party.PartyPlusPlus;
import io.github.zap.party.party.Party;
import io.github.zap.party.party.PartyManager;

public class KickOfflineMembersForm extends CommandForm<Party> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("kickoffline")
    };

    private static final CommandValidator<Party, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();
        Party party = partyManager.getPartyForPlayer(previousData);

        if (party == null) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        if (!party.isOwner(previousData)) {
            return ValidationResult.of(false, "You are not the party owner.", null);
        }

        return ValidationResult.of(true, null, party);
    }, Validators.PLAYER_EXECUTOR);

    public KickOfflineMembersForm() {
        super("Kicks a member from your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        PartyPlusPlus.getInstance().getPartyManager().kickOffline(data);
        return null;
    }


}
