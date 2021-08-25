package io.github.zap.party.plugin;

import io.github.regularcommands.commands.CommandManager;
import io.github.zap.party.command.PartyCommand;
import io.github.zap.party.party.Party;
import io.github.zap.party.party.chat.BasicPartyChatHandler;
import io.github.zap.party.party.invitation.TimedInvitationManager;
import io.github.zap.party.party.list.BasicPartyLister;
import io.github.zap.party.party.list.PartyLister;
import io.github.zap.party.party.member.PartyMember;
import io.github.zap.party.party.namer.OfflinePlayerNamer;
import io.github.zap.party.party.namer.SingleTextColorOfflinePlayerNamer;
import io.github.zap.party.party.settings.PartySettings;
import io.github.zap.party.plugin.tracker.PartyTracker;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.logging.Level;

/**
 * ZAP implementation of {@link PartyPlusPlus}.
 */
public class PartyPlugin extends JavaPlugin implements Listener, PartyPlusPlus {

    private PartyTracker partyTracker;

    @SuppressWarnings("FieldCanBeLocal")
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        StopWatch timer = StopWatch.createStarted();

        initPartyTracker();
        initCommands(MiniMessage.get());

        Bukkit.getPluginManager().registerEvents(this, this);

        timer.stop();
        this.getLogger().log(Level.INFO, String.format("Enabled successfully; ~%sms elapsed.", timer.getTime()));
    }

    /**
     * Initializes the party manager
     */
    private void initPartyTracker() {
        this.partyTracker = new PartyTracker();
    }

    /**
     * Registers the command manager
     */
    private void initCommands(@NotNull MiniMessage miniMessage) {
        this.commandManager = new CommandManager(this);

        Random random = new Random();
        OfflinePlayerNamer playerNamer = new SingleTextColorOfflinePlayerNamer();
        PartyLister partyLister = new BasicPartyLister(this, miniMessage,
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.GREEN),
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.RED),
                new SingleTextColorOfflinePlayerNamer(NamedTextColor.BLUE));
        this.commandManager.registerCommand(new PartyCommand(this.partyTracker,
                owner -> new Party(miniMessage, random, new PartyMember(owner),
                        new PartySettings(), PartyMember::new,
                        new TimedInvitationManager(this, miniMessage, playerNamer),
                        new BasicPartyChatHandler(PartyPlugin.this, miniMessage), partyLister, playerNamer)));
    }

    @Override
    public @NotNull PartyTracker getPartyTracker() {
        return this.partyTracker;
    }

}
