package io.github.zap.event;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PlayerRightClickEvent extends CustomEvent {
    private final Player player;
    private final Block clicked;
    private final ItemStack heldItem;
}
