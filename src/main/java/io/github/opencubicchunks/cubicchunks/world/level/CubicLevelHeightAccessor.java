package io.github.opencubicchunks.cubicchunks.world.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;

import javax.annotation.Nullable;

/**
 * Extension interface for levels that support cubic chunks.
 * This is injected into Level via Mixin.
 * 
 * NOTE: Does NOT extend LevelHeightAccessor to avoid default method conflicts.
 * Height overrides are handled via @Inject in the mixins.
 */
public interface CubicLevelHeightAccessor {
    
    // Constants for cubic world height
    int CUBIC_MIN_HEIGHT = Integer.MIN_VALUE + 64;
    int CUBIC_MAX_HEIGHT = Integer.MAX_VALUE - 64;
    int CUBIC_HEIGHT = Integer.MAX_VALUE - 128;
    
    /**
     * Check if this level uses cubic chunks.
     */
    boolean isCubic();
    
    /**
     * Enable or disable cubic mode for this level.
     */
    void setCubic(boolean cubic);
    
    /**
     * Get a cube at the given position.
     */
    @Nullable
    Cube getCube(CubePos pos);
    
    /**
     * Get a cube at the given position, loading it if necessary.
     */
    @Nullable
    Cube getOrLoadCube(CubePos pos);
    
    /**
     * Check if a cube is loaded.
     */
    boolean hasCube(CubePos pos);
}