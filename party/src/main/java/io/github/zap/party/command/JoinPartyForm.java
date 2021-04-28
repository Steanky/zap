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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Joins a player's party if it exists
 */
public class JoinPartyForm extends CommandForm<Party> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("join"),
            new Parameter("\\w+")
    };

    private static final CommandValidator<Party, ?> VALIDATOR
            = new CommandValidator<>(((context, arguments, previousData) -> {
        String ownerName = (String) arguments[1];
        OfflinePlayer owner = Bukkit.getOfflinePlayerIfCached(ownerName);

        if (owner == null) {
            return ValidationResult.of(false, String.format("%s is not online!", ownerName), null);
        }

        ownerName = owner.getName(); // change any capitalization

        Party party = PartyPlusPlus.getInstance().getPartyManager().getPartyForPlayer(owner);

        if (party == null) {
            return ValidationResult.of(false, String.format("%s is not in a party.", owner), null);
        }

        if (!(party.getInvites().contains((OfflinePlayer) context.getSender())
                || party.getPartySettings().isAnyoneCanJoin())) {
            return ValidationResult.of(false, String.format("You don't have an invite to %s's party!", ownerName),
                    null);
        }

        return ValidationResult.of(true, null, party);
    }), Validators.PLAYER_EXECUTOR);

    public JoinPartyForm() {
        super("Joins a party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        Player sender = (Player) context.getSender();

        data.addMember(sender);

        return null;
    }

}
