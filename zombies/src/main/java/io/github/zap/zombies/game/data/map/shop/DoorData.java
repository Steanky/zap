package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.Getter;

import java.util.List;

@Getter
public class DoorData extends ShopData {

    private MultiBoundingBox doorBounds;

    private List<DoorSide> doorSides;

    private DoorData() {

    }

}
