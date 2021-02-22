package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a side of a door
 */
@Getter
@AllArgsConstructor
public class DoorSide {
   private int cost = 0;

   private List<String> opensTo = new ArrayList<>();

   private BoundingBox triggerBounds = new BoundingBox();

   private Vector hologramLocation;

   public DoorSide(Vector hologramLocation) {
      this.hologramLocation = hologramLocation;
   }
}
