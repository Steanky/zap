package io.github.zap.arenaapi.anticheat;

import io.github.zap.arenaapi.ArenaApi;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerInteractionManager implements Listener {

    private final AntiCheat antiCheat;

    private final Map<Player, long[]> interactionMap = new HashMap<>();

    public PlayerInteractionManager(AntiCheat antiCheat) {
        this.antiCheat = antiCheat;

        Bukkit.getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        long[] interactions = interactionMap.computeIfAbsent(event.getPlayer(), player -> {
            /*
             * index 0 - current interaction number
             * index 1 - last interaction time
             * index 2-20 - interaction millisecond interval
             */
            long[] newInteraction = new long[21];
            newInteraction[0] = 1;

            return newInteraction;
        });

        int index = (int) interactions[0];
        long currentTime = System.currentTimeMillis();
        if (index != 1) {
            interactions[index] = currentTime - interactions[1];
        }

        if (index == 20) {
            interactionMap.remove(event.getPlayer());

            if (antiCheat.isUsingAutoClicker(Arrays.copyOfRange(interactions, 2, interactions.length))) {
                event.getPlayer().sendMessage(Component.text("are you using autoclicker?"));
            }
        } else {
            interactions[0]++;
            interactions[1] = currentTime;
        }
    }

}
