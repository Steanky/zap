package io.github.zap.zombies.game2.scoreboard.writer;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SidebarLineWriter {

    private final static ChatColor[] TEXT_FORMATTING = ChatColor.values();

    private final List<Team> teams = new ArrayList<>();

    private final Objective objective;

    public SidebarLineWriter(@NotNull Objective objective) {
        this.objective = objective;
    }

    // Don't believe Yui's comment, it's distracting you from the black magic we have in our code
    // Considered making a number utils class
    private static int digitAt(int num, int index, int base) {
        return (int) (num / Math.pow(base, index)) % base;
    }

    public @NotNull Optional<Component> line(int index) {
        Team team = teams.get(index);
        if (team != null) {
            return Optional.of(team.prefix());
        }

        return Optional.empty();
    }

    public void line(int index, @NotNull Component component) {
        while (teams.size() <= index) {
            Scoreboard scoreboard = Objects.requireNonNull(objective.getScoreboard(),
                    "Tried to update line for unregistered scoreboard!");

            Team team = scoreboard.registerNewTeam(objective.getName() + "-" + teams.size());
            String entry = nextEntry();
            team.addEntry(entry);
            objective.getScore(entry).setScore(-1 - teams.size());

            teams.add(team);
        }

        teams.get(index).prefix(component);
    }

    public void clear() {
        Scoreboard scoreboard = Objects.requireNonNull(objective.getScoreboard(),
                "Tried to clear lines for unregistered scoreboard!");
        for (Team team : teams) {
            for (String entry : team.getEntries()) {
                scoreboard.resetScores(entry);
            }

            team.unregister();
        }

        teams.clear();
    }

    // Why does Yui always perform magic?
    // Pretty magic code doc later
    private @NotNull String nextEntry() {
        int entryIndex = teams.size() + 2;
        List<String> entries = new ArrayList<>();
        for(int i = 0; i < 40; i++) {
            int digit = digitAt(entryIndex, i, TEXT_FORMATTING.length + 1);
            entries.add(digit == 0 ? "" : "" + TEXT_FORMATTING[digit - 1]);

            // Check for early break. If the number smallest number have i + 1 in length bigger than entryIndex
            if (Math.pow(TEXT_FORMATTING.length + 1, i + 1) > entryIndex) {
                break;
            }
        }

        // Since we add in from right to left (easier with the base calculation) we have to reverse our string to get
        // the desired output
        Collections.reverse(entries);
        return String.join("", entries);
    }

}
