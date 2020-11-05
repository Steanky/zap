package io.github.zap.event.player;

import io.github.zap.event.CustomEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PlayerRightClickEvent extends CustomEvent {
    Player player;
    Block clicked;
    ItemStack heldItem;
    Action action;
}