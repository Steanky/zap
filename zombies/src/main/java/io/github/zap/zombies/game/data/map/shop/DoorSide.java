package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.game.MultiBoundingBox;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a side of a door
 */
@Getter
public class DoorSide {

   private int cost = 0;

   private List<String> opensTo = new ArrayList<>();

   private MultiBoundingBox triggerBounds = new MultiBoundingBox();

   private Vector hologramLocation;

   public DoorSide(Vector hologramLocation) {
      this.hologramLocation = hologramLocation;
   }

}
