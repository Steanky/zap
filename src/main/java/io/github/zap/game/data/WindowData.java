package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
    private int area;

    @Getter
    private MultiBoundingBox interiorBounds;

    @Getter
    private BoundingBox faceBounds;

    @Getter
    private Vector base;

    @Getter
    @Setter
    @NoSerialize
    private int lastRepaired = 0;

    private WindowData() {}
}
