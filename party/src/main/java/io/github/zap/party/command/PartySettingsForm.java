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
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Optional;

// TODO: far post-beta, allow for settings to be more flexible
public class PartySettingsForm extends CommandForm<Pair<Party, Object[]>> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("settings"),
            new Parameter(".*", "[setting-name]"),
            new Parameter(".*", "[setting-value]")
    };

    private static final CommandValidator<Pair<Party, Object[]>, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        Optional<Party> partyOptional = PartyPlusPlus.getInstance().getPartyManager()
                .getPartyForPlayer(previousData);
        if (partyOptional.isEmpty()) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        Party party = partyOptional.get();

        if (!party.isOwner(previousData)) {
            return ValidationResult.of(false, "You are not the party owner.", null);
        }

        return ValidationResult.of(true, null,
                Pair.of(party, Arrays.copyOfRange(arguments, 1, arguments.length)));
    }, Validators.PLAYER_EXECUTOR);

    public PartySettingsForm() {
        super("Modifies party settings.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<Pair<Party, Object[]>, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Pair<Party, Object[]> data) {
        Party party = data.getLeft();
        Object[] parameters = data.getRight();
        String settingName = ((String) parameters[0]).toLowerCase();

        switch (settingName) {
            case "allinvite":
                party.getPartySettings().setAllInvite(Boolean.parseBoolean((String) parameters[1]));
                return String.format(">gold{Set allinvite to %s!}",
                        party.getPartySettings().isAllInvite() ? "ON" : "OFF");
            case "anyonecanjoin":
                party.getPartySettings().setAnyoneCanJoin(Boolean.parseBoolean((String)parameters[1]));
                return String.format(">gold{Set anyonecanjoin to %s!}",
                        party.getPartySettings().isAnyoneCanJoin() ? "ON" : "OFF");
            case "inviteexpirationtime":
                try {
                    party.getPartySettings().setInviteExpirationTime(Long.parseLong((String) parameters[1]));
                    return String.format(">gold{Set inviteexpirationtime to %d!}",
                            party.getPartySettings().getInviteExpirationTime());
                } catch (NumberFormatException e) {
                    return ">red{Invalid invite expiration time!}";
                }
        }

        return null;
    }

}
