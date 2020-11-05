package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

@AllArgsConstructor
public class WindowData extends DataSerializable {
    @Getter
    private List<Material> repairedMaterials;

    @Getter
    private List<Material> brokenMaterials;

    @Getter
    private List<Vector> faceVectors;

    @Getter
    private BoundingBox faceBounds;

    @Getter
    private MultiBoundingBox interiorBounds;

    @Getter
    private Vector base;

    @NoSerialize
    private Vector centerVector;

    private WindowData() {}

    public Vector getCenter() {
        if(centerVector == null) {
            centerVector = faceBounds.getCenter();
        }

        return centerVector.clone();
    }
}
