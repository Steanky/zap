package io.github.zap.zombies.game.util;

import io.github.zap.zombies.Zombies;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Class that represents a set of game sounds as a playable jingle
 */
@Getter
public class Jingle {

    private final transient Zombies zombies;

    private List<ImmutablePair<List<Note>, Long>> notePairs;

    /**
     * Plays the jingle
     * @param location The location to play the jingle at
     */
    public void play(Location location) {
        playAt(location, 0);
    }

    /**
     * Plays the noteNumberth note of the jingle
     * @param location The location to play the note at
     * @param noteNumber The note number in the jingle
     */
    public void playAt(Location location, int noteNumber) {
        if (noteNumber < notePairs.size()) {
            World world = location.getWorld();
            ImmutablePair<List<Note>, Long> notePair = notePairs.get(noteNumber);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Note note : notePair.getLeft()) {
                        world.playSound(
                                location,
                                note.getSound(),
                                note.getVolume(),
                                note.getPitch()
                        );
                    }
                    playAt(location, noteNumber + 1);

                    onNotePlayed();
                }
            }.runTaskLater(zombies, 20 * notePair.getRight());
        } else {
            onEnd();
        }
    }

    /**
     * Method called when a note of the jingle is played
     */
    protected void onNotePlayed() {

    }

    /**
     * Method called upon jingle completion
     */
    protected void onEnd() {

    }

    protected Jingle() {
        zombies = Zombies.getInstance();
    }

    /**
     * A single note of a jingle
     */
    @Getter
    private static class Note {

        private Sound sound;

        private float volume;

        private float pitch;

        private Note() {

        }

    }

}
