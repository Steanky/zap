package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Data for a side of a door
 */
@Getter
public class DoorSide {

   private int cost;

   private List<String> opensTo;

   private MultiBoundingBox triggerBounds;

   private Vector hologramLocation;

   private DoorSide() {

   }

}
