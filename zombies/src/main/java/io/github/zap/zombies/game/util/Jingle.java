package io.github.zap.zombies.game.util;

import io.github.zap.zombies.Zombies;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Utility class to play sets of notes
 */
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
    public static void play(List<Note> jingle, JingleListener jingleListener,
                            Location location) {
        play(jingle, jingleListener, location, 0);
    }

    /**
     * Plays the soundNumberth note of the jingle
     * @param location The location to play the note at
     * @param soundNumber The note number in the jingle
     */
    public static void play(List<Note> jingle, JingleListener jingleListener,
                            Location location, int soundNumber) {
        if (soundNumber < jingle.size()) {
            if (soundNumber == 0) {
                jingleListener.onStart(jingle);
            }

            World world = location.getWorld();
            Note note = jingle.get(soundNumber);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Sound sound : note.getSounds()) {
                        world.playSound(sound, location.getX(), location.getY(), location.getZ());
                    }
                    jingleListener.onNotePlayed(jingle);

                    play(jingle, jingleListener, location, soundNumber + 1);
                }
            }.runTaskLater(zombies, note.getLength());
        } else {
            jingleListener.onEnd(jingle);
        }
    }

    @Getter
    public static class Note {
        List<Sound> sounds;
        long length;

        public Note() {

        }

    }

    /**
     * Listener for parts of a jingle being played
     */
    public interface JingleListener {

        /**
         * Method called when the jingle playing begins
         */
        default void onStart(List<Note> jingle) {

        }

        /**
         * Method called when a note of the jingle is played
         */
        default void onNotePlayed(List<Note> jingle) {

        }

        /**
         * Method called upon jingle completion
         */
        default void onEnd(List<Note> jingle) {

        }
    }

}
