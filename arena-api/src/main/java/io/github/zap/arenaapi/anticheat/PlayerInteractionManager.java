package io.github.zap.arenaapi.anticheat;

import io.github.zap.arenaapi.ArenaApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayerInteractionManager implements Listener {

    /*
     * index 0 - current interaction number
     * index 1 - last interaction time
     * index 2-20 - interaction millisecond interval
     */
    private static final long[] INTERACTION_ARRAY = new long[]{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    private final AntiCheat antiCheat;

    private final Map<Player, long[]> interactionMap = new HashMap<>();

    public PlayerInteractionManager(AntiCheat antiCheat) {
        this.antiCheat = antiCheat;

        Bukkit.getPluginManager().registerEvents(this, ArenaApi.getInstance());
    }

    private long[] createDefaultInteractions() {
        long[] array = new long[21];
        System.arraycopy(INTERACTION_ARRAY, 0, array, 0, INTERACTION_ARRAY.length);

        return array;
    }

    private long[] getInteractionsForPlayer(@NotNull Player player) {
        return interactionMap.computeIfAbsent(player, unused -> createDefaultInteractions());
    }

    private void onClick(@NotNull Player player) {
        long[] interactions = getInteractionsForPlayer(player);

        int index = (int) interactions[0];
        long currentTime = System.currentTimeMillis();
        if (index != 1) {
            interactions[index] = currentTime - interactions[1];
        }

        if (index == 20) {
            interactionMap.remove(player);

            if (antiCheat.isUsingAutoClicker(Arrays.copyOfRange(interactions, 2, interactions.length))) {
                suspectPlayer(player, ReportType.AUTOCLICKER);
            }
        } else {
            interactions[0]++;
            interactions[1] = currentTime;
        }
    }

    private void suspectPlayer(@NotNull Player player, @NotNull ReportType reportType) {

    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        onClick(event.getPlayer());
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        onClick(event.getPlayer());
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        onClick(event.getPlayer());
    }

}
