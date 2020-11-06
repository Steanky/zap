package io.github.zap.game.data;

import io.github.zap.game.MultiAccessor;
import io.github.zap.game.MultiBoundingBox;
import io.github.zap.game.arena.ZombiesPlayer;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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

    @NoSerialize
    private int volume = -1;

    @Getter
    @NoSerialize
    private final MultiAccessor<Integer> currentIndexAccessor = new MultiAccessor<>(0);

    @Getter
    @NoSerialize
    private final MultiAccessor<ZombiesPlayer> repairingPlayer = new MultiAccessor<>(null);

    @Getter
    @NoSerialize
    private final MultiAccessor<Entity> attackingEntity = new MultiAccessor<>(null);

    private WindowData() {}

    public Vector getCenter() {
        if(centerVector == null) {
            centerVector = faceBounds.getCenter();
        }

        return centerVector.clone();
    }

    public int getVolume() {
        if(volume == -1) {
            volume = (int)faceBounds.getVolume();
        }

        return volume;
    }
}
