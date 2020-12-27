package io.github.zap.zombies.game.shop;

import lombok.Getter;

public enum ShopType {

    GUN_SHOP("GUN_SHOP");

    @Getter
    private final String name;

    ShopType(String name) {
        this.name = name;
    }

}
