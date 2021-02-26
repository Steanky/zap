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
   private final int cost = 0;

   private final List<String> opensTo = new ArrayList<>();

   private final BoundingBox triggerBounds = new BoundingBox();

   private Vector hologramLocation;

   private DoorSide() {}

   public DoorSide(Vector hologramLocation) {
      this.hologramLocation = hologramLocation;
   }
}
