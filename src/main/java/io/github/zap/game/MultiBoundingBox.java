package io.github.zap.game;

import io.github.zap.serialize.DataSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class MultiBoundingBox extends DataSerializable {
    private final Set<BoundingBox> boundingBoxes = new HashSet<>();

    public void addBounds(BoundingBox boundingBox) {
        boundingBoxes.add(boundingBox);
    }

    public boolean insideAny(Vector vector) {
        for(BoundingBox boundingBox : boundingBoxes) {
            if(boundingBox.contains(vector)) {
                return true;
            }
        }

        return false;
    }
}
