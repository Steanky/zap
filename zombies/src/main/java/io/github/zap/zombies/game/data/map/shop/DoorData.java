package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a door
 */
@Getter
public class DoorData extends ShopData {

    private MultiBoundingBox doorBounds = new MultiBoundingBox();

    private List<DoorSide> doorSides = new ArrayList<>();

    private DoorData() {

    }

}
