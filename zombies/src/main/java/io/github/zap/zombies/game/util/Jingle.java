package io.github.zap.zombies.game.util;

import io.github.zap.zombies.Zombies;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Utility class to play sets of notes
 */
@Getter
public class Jingle {

    private final static Zombies zombies;

    static {
        zombies = Zombies.getInstance();
    }

    private Jingle() {

    }

    /**
     * Plays a set of notes
     * @param jingle The notes to play
     * @param location The location to play the jingle at
     */
    public static void play(List<Pair<List<Note>, Long>> jingle, JingleListener jingleListener,
                            Location location) {
        playAt(jingle, jingleListener, location, 0);
    }

    /**
     * Plays the noteNumberth note of the jingle
     * @param location The location to play the note at
     * @param noteNumber The note number in the jingle
     */
    public static void playAt(List<Pair<List<Note>, Long>> jingle, JingleListener jingleListener,
                              Location location, int noteNumber) {
        if (noteNumber < jingle.size()) {
            if (noteNumber == 0) {
                jingleListener.onStart(jingle);
            }

            World world = location.getWorld();
            Pair<List<Note>, Long> notePair = jingle.get(noteNumber);

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
                    playAt(jingle, jingleListener, location, noteNumber + 1);

                    jingleListener.onNotePlayed(jingle);
                }
            }.runTaskLater(zombies, 20 * notePair.getRight());
        } else {
            jingleListener.onEnd(jingle);
        }
    }

    /**
     * A single note of a jingle
     */
    @Getter
    public static class Note {

        Sound sound;

        float volume;

        float pitch;

        private Note() {

        }
    }

    /**
     * Listener for parts of a jingle being played
     */
    public interface JingleListener {

        /**
         * Method called when the jingle playing begins
         */
        default void onStart(List<Pair<List<Note>, Long>> jingle) {

        }

        /**
         * Method called when a note of the jingle is played
         */
        default void onNotePlayed(List<Pair<List<Note>, Long>> jingle) {

        }

        /**
         * Method called upon jingle completion
         */
        default void onEnd(List<Pair<List<Note>, Long>> jingle) {

        }
    }

}
