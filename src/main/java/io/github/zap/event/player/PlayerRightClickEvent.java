package io.github.zap.event.player;

import io.github.zap.event.CustomEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class PlayerRightClickEvent extends CustomEvent {
    private final Player player;
    private final Block clicked;
    private final ItemStack heldItem;
    private final Action action;
}