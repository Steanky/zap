package io.github.zap.game.arena;

import io.github.zap.event.player.PlayerRightClickEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

@RequiredArgsConstructor
public class ZombiesPlayer {
    @Getter
    private final Player player;

    @Getter
    private final ZombiesArena arena;

    @Getter
    @Setter
    private PlayerState state;

    @Getter
    private int coins;

    private int repairTick = 0;

    public void playerTick() {

    }

    public void playerRightClick(PlayerRightClickEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            arena.tryOpenDoor(this, event.getClicked().getLocation().toVector());
        }
    }

    public boolean canPurchase(Purchasable purchasable) {
        return state == PlayerState.ALIVE && purchasable.getCost() <= coins;
    }

    public void giveCoins(int amount) {
        coins += amount;
    }
}
