package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.shop.Shop;
import lombok.Getter;

public class JacksonShopManager implements ShopManager {

    @Getter
    private final ShopDataDeserializer shopDataDeserializer = new ShopDataDeserializer();

    @Getter
    private final ShopCreator shopCreator = new ShopCreator();

    @Override
    public <D extends ShopData> void addShop(String shopType, Class<D> dataClass,
                                             ShopCreator.ShopMapping<D> shopMapping) {
        shopDataDeserializer.getShopDataClassMappings().put(shopType, dataClass);
        shopCreator.getShopMappings().put(shopType, shopMapping);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ShopData> Shop<D> createShop(ZombiesArena zombiesArena, D shopData) {
        ShopCreator.ShopMapping<D> shopMapping
                = (ShopCreator.ShopMapping<D>) shopCreator.getShopMappings().get(shopData.getType());
        return shopMapping.createShop(zombiesArena, shopData);
    }

}
