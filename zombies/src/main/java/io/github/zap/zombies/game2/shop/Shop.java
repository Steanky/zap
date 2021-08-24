package io.github.zap.zombies.game2.shop;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public abstract class Shop {

    private final World world;

    private boolean visible = false;

    private boolean powered = false;

    public Shop(@NotNull World world) {
        this.world = world;
    }



    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public abstract @NotNull String getShopType();

}
