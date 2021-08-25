package io.github.zap.party.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.party.party.Party;
import io.github.zap.party.plugin.tracker.PartyTracker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Joins a player's party if it exists
 */
public class JoinPartyForm extends CommandForm<Party> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("join"),
            new Parameter("\\w+", "[owner-name]")
    };

    private final CommandValidator<Party, ?> validator;

    public JoinPartyForm(@NotNull PartyTracker partyTracker) {
        super("Joins a party.", Permissions.NONE, PARAMETERS);
        this.validator = new CommandValidator<>(((context, arguments, previousData) -> {
            if (partyTracker.getPartyForPlayer(previousData).isPresent()) {
                return ValidationResult.of(false,
                        "You are already in a party! Leave it to join another one.", null);
            }

            String ownerName = (String) arguments[1];
            if (previousData.getName().equalsIgnoreCase(ownerName)) {
                return ValidationResult.of(false, "You cannot join your own party.", null);
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayerIfCached(ownerName);
            if (owner == null) {
                return ValidationResult.of(false, String.format("%s is not registered on the server!", ownerName),
                        null);
            }

            ownerName = owner.getName(); // change any capitalization

            Optional<Party> partyOptional = partyTracker.getPartyForPlayer(owner);
            if (partyOptional.isEmpty()) {
                return ValidationResult.of(false, String.format("%s is not in a party.", ownerName), null);
            }

            Party party = partyOptional.get();
            if (!(party.getInvitationManager().hasInvitation(previousData)
                    || party.getPartySettings().isAnyoneCanJoin())) {
                return ValidationResult.of(false, String.format("You don't have an invite to %s's party!",
                        ownerName), null);
            }

            return ValidationResult.of(true, null, party);
        }), Validators.PLAYER_EXECUTOR);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return this.validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        data.addMember((Player) context.getSender());
        return null;
    }

}
