package io.github.zap.zombies.game.data.map.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Data for a side of a door
 */
@Getter
@AllArgsConstructor
public class DoorSide {
   private int cost;

   private List<String> opensTo;

   private BoundingBox triggerBounds;

   private Vector hologramLocation;

   private DoorSide() { }
}
