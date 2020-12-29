package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.Value;

@Value
public class ShopEventArgs {

    Shop<?> shop;

    ZombiesPlayer zombiesPlayer;

}
