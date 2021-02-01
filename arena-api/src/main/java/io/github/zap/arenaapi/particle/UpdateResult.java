package io.github.zap.arenaapi.particle;

import lombok.Value;

/**
 * Used to represent a calculation that invalidates a certain range of values for an array of FragmentData.
 */
@Value
public class UpdateResult {
    FragmentData[] newData;
    int startIndexInclusive;
    int replaceRange;
}
