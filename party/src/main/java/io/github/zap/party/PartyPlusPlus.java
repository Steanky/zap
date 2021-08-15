package io.github.zap.party;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.party.command.PartyCommand;
import io.github.zap.party.party.Party;
import io.github.zap.party.party.PartyMember;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Creates cool parties for you!
 */
public class PartyPlusPlus extends JavaPlugin implements Listener {

    @Getter
    private static PartyPlusPlus instance;

    private Map<UUID, Party> partyMap;

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();

        instance = this;

        initPartyMap();
        initCommands();

        Bukkit.getPluginManager().registerEvents(this, this);

        timer.stop();
        instance.getLogger().log(Level.INFO, String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    /**
     * Initializes the party manager
     */
    private void initPartyMap() {
        this.partyMap = new HashMap<>();
    }

    /**
     * Registers the command manager
     */
    private void initCommands() {
        commandManager = new CommandManager(this);
        commandManager.registerCommand(new PartyCommand());
    }

    /**
     * Starts tracking a party
     * @param party The party to track
     */
    public void trackParty(@NotNull Party party) {
        party.registerJoinHandler(member -> {
            OfflinePlayer player = member.getOfflinePlayer();
            if (this.partyMap.containsKey(player.getUniqueId())) {
                this.partyMap.get(player.getUniqueId()).removeMember(player, false);
            }

            this.partyMap.put(member.getOfflinePlayer().getUniqueId(), party);
        });
        party.registerLeaveHandler(member -> this.partyMap.remove(member.getOfflinePlayer().getUniqueId()));

        for (PartyMember member : party.getMembers()) {
            this.partyMap.put(member.getOfflinePlayer().getUniqueId(), party);
        }
    }

    /**
     * Gets the party a player is in
     * @param player The player to check
     * @return An optional of their party
     */
    public @NotNull Optional<Party> getPartyForPlayer(@NotNull OfflinePlayer player) {
        return Optional.ofNullable(this.partyMap.get(player.getUniqueId()));
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        this.getPartyForPlayer(event.getPlayer()).ifPresent(party -> party.onAsyncChat(event));
    }

}
