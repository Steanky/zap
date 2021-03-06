package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.ModifierModificationPowerUpData;
import io.github.zap.zombies.game.util.MathUtils;

/**
 * Applies the modifier data to the amount player gold at the time
 * of activation
 */
@PowerUpType(name = "Player-Gold-Modification")
public class PlayerGoldModificationPowerUp extends PowerUp{
    public PlayerGoldModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public PlayerGoldModificationPowerUp(ModifierModificationPowerUpData data, ZombiesArena arena, int refreshRate) {
        super(data, arena, refreshRate);
    }

    @Override
    public void activate() {
        getArena().getPlayerMap().forEach((l,r) -> {
            var cData = (ModifierModificationPowerUpData)getData();
            var newCoin = r.getCoins() * cData.getMultiplier() + cData.getAmount();
            var coinDifference = (int) (newCoin - r.getCoins());
            if(coinDifference > 0) {
                r.addCoins(coinDifference, getData().getDisplayName());
            } else {
                // The clamp function prevent player to have negative coins
                r.subtractCoins((int) MathUtils.clamp(-coinDifference, 0, r.getCoins()));
            }
        });
    }
}
