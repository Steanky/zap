package io.github.zap.event;

import io.github.zap.ZombiesPlugin;
import io.github.zap.event.player.PlayerRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EventProxy implements Listener {
    public EventProxy() {
        ZombiesPlugin zombiesPlugin = ZombiesPlugin.getInstance();
        zombiesPlugin.getServer().getPluginManager().registerEvents(this, zombiesPlugin);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if(event.getHand() == EquipmentSlot.HAND && (action == Action.RIGHT_CLICK_BLOCK ||
                action == Action.RIGHT_CLICK_AIR)) {
            ZombiesPlugin.getInstance().getServer().getPluginManager().callEvent(new PlayerRightClickEvent(
                    event.getPlayer(), event.getClickedBlock(), event.getItem(), action));
        }
    }
}
