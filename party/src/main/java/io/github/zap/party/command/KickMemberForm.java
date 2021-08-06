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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

/**
 * Kicks a member from the party
 */
public class KickMemberForm extends CommandForm<OfflinePlayer> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("kick"),
            new Parameter("\\w+", "[player-name]")
    };

    private static final CommandValidator<OfflinePlayer, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();

        Optional<Party> partyOptional = partyManager.getPartyForPlayer(previousData);
        if (partyOptional.isEmpty()) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        Party party = partyOptional.get();

        if (!party.isOwner(previousData)) {
            return ValidationResult.of(false, "You are not the party owner.", null);
        }

        String playerName = (String) arguments[1];
        if (previousData.getName().equalsIgnoreCase(playerName)) {
            return ValidationResult.of(false, "You cannot kick yourself.", null);
        }

        OfflinePlayer toKick = Bukkit.getOfflinePlayerIfCached(playerName);
        if (toKick == null) {
            return ValidationResult.of(false, String.format("%s is not registered on the server!", playerName),
                    null);
        }

        Optional<Party> toKickPartyOptional = partyManager.getPartyForPlayer(toKick);
        if (toKickPartyOptional.isPresent()) {
            if (party.equals(toKickPartyOptional.get())) {
                return ValidationResult.of(true, null, toKick);
            }
        }

        return ValidationResult.of(false, String.format("%s is not in your party.", playerName), null);

    }, Validators.PLAYER_EXECUTOR);

    public KickMemberForm() {
        super("Kicks a member from your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<OfflinePlayer, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, OfflinePlayer data) {
        PartyPlusPlus.getInstance().getPartyManager().removePlayerFromParty(data, true);
        return null;
    }

}
