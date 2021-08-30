package io.github.zap.arenaapi.pathfind.path;

import io.github.zap.vector.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single node along a {@link io.github.zap.arenaapi.pathfind.agent.PathAgents}'s path, which may be
 * "connected" to exactly one other node (its parent). Thus, PathNode objects may be used as a singly-linked list. The
 * "ordering" of this list is not determined, and may either point from the start of the path to its end, the end to
 * its start, or some other scheme which must be described by the method that produces the PathNode.
 *
 * PathNode objects, as they inherit from Vector3I, must satisfy the general contract that the values returned by x(),
 * y(), and z() must stay constant for the lifetime of the object. However, this immutability does not extend to
 * getOffsetVector() or parent(). Indeed, these values should be <i>expected</i> to change over time.
 *
 * There are no publicly accessible factory methods that instantiate a default PathNode implementation. Instead,
 * {@link io.github.zap.arenaapi.pathfind.operation.PathOperation} objects are expected to create and use their own
 * custom implementations of PathNode.
 */
public interface PathNode extends Vector3I {
    /**
     * Sets the "offset vector" of this PathNode object. When this object is converted to an NMS PathPoint, the
     * position of the resulting PathPoint will be equal to the sum of the positions of the PathNode and the offset
     * vector. This is required in some cases where certain NMS methods may expect PathPoints to be positioned
     * differently than the PathNode from which they are produced.
     * @param vector The offset vector, whose components must be finite
     */
    void setOffsetVector(@NotNull Vector3I vector);

    /**
     * Returns the offset vector for this PathNode. See
     * {@link io.github.zap.arenaapi.pathfind.path.PathNode#setOffsetVector(Vector3I)} for more information about how
     * the offset vector should be interpreted.
     * @return The offset vector of this PathNode. Typically, defaults to {@link io.github.zap.vector.Vectors#ZERO},
     * but is not required to do so
     */
    @NotNull Vector3I getOffsetVector();

    /**
     * Returns the parent node of this PathNode, which may be used to traverse PathNode objects as one normally would
     * traverse a linked list.
     * @return The parent PathNode, or null if there are none
     */
    @Nullable PathNode parent();
}
