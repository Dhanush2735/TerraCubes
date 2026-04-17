package io.github.opencubicchunks.cubicchunks.world.level.chunk;

import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;

import javax.annotation.Nullable;

/**
 * Interface for accessing cubes within a chunk column.
 * Injected into ChunkAccess via Mixin.
 */
public interface CubeAccess {
    
    /**
     * Get a cube at the given Y level.
     */
    @Nullable
    Cube getCube(int cubeY);
    
    /**
     * Set a cube at the given Y level.
     */
    void setCube(int cubeY, Cube cube);
    
    /**
     * Remove a cube at the given Y level.
     */
    void removeCube(int cubeY);
    
    /**
     * Get all cubes in this column.
     */
    Iterable<Cube> getCubes();
    
    /**
     * Check if this chunk has any cubes loaded.
     */
    boolean hasCubes();
}
