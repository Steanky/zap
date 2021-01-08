package io.github.zap.zombies.command.mapeditor;

import com.google.common.collect.Lists;
import io.github.zap.zombies.Zombies;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class ContextManager implements Listener {
    private final Map<UUID, EditorContext> contextMap = new HashMap<>();
    private final Material itemType = Material.STICK;
    private final List<String> itemLore = Lists.newArrayList("Zombies Map Editor Wand[TM]");
    private final ItemStack editorItem = new ItemStack(itemType);

    public ContextManager() {
        Zombies zombies = Zombies.getInstance();
        zombies.getServer().getPluginManager().registerEvents(this, zombies);
        editorItem.setLore(itemLore);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();
            if(item != null && block != null && item.getType() == itemType) {
                EditorContext context = contextMap.get(event.getPlayer().getUniqueId());

                if(context != null && itemLore.equals(item.getLore())) {
                    context.handleClicked(block);
                }
            }
        }
    }
}
