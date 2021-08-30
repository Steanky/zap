package io.github.zap.arenaapi.pathfind.agent;

import io.github.zap.vector.Vector3D;

/**
 * Used to represent a navigation-capable object.
 *
 * This interface extends {@link Vector3D}, and thus inherits the general contract that its reported position must be
 * constant for the lifetime of the object. It also provides additional methods' width(), height(), jumpHeight(), and
 * fallTolerance(), which may be used by pathfinding implementations to determine "navigability" of a space (whether
 * this agent may stand at or walk to a given block position without colliding).
 *
 * A common use case for a PathAgent is representing a Bukkit {@link org.bukkit.entity.Mob}, and is in fact designed to
 * easily facilitate this. Similarly to entity hitboxes, Z and X width cannot differ.
 *
 * See {@link PathAgents} for several helper functions that may be used to instantiate default implementations of this
 * interface.
 */
public interface PathAgent extends Vector3D {
    /**
     * Gets the precise width of this PathAgent, which is taken to be the same for both the X and Z axis. Furthermore,
     * this value is assumed to be greater than zero and finite.
     * @return The width of this PathAgent
     */
    double width();

    /**
     * Gets the height of this PathAgent. This value is assumed to be greater than zero and finite.
     * @return The height of this PathAgent
     */
    double height();

    /**
     * Gets the jump height of this PathAgent, which may be used by pathfinding implementations for walk-based
     * movement. This value is assumed to be greater than or equal to zero and finite.
     * @return The jump height of this PathAgent
     */
    double jumpHeight();

    /**
     * Gets the fall tolerance of this PathAgent, which is interpreted as the maximum allowable fall height this agent
     * will tolerate. That is, it is the maximum number of blocks pathfinders may take into consideration before
     * treating a node as impassible. This value is assumed to be greater than zero, or equal to
     * Double.POSITIVE_INFINITY
     * @return The fall tolerance of this PathAgent
     */
    double fallTolerance();
}
