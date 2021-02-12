package io.github.zap.arenaapi.data;

/**
 * General interface for a object that provides access to containers.
 */
public interface ContainerProvider {
    /**
     * Gets the ContainerManager that provides access to this object's containers.
     */
    ContainerManager getContainerManager();
}