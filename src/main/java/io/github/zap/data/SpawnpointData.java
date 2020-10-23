package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

@AllArgsConstructor
@Getter
public class SpawnpointData extends DataSerializable {
    private Vector origin;
    private Vector target;

    private SpawnpointData() {}
}
