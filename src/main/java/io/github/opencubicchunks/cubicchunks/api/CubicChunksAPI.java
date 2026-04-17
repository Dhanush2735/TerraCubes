package io.github.opencubicchunks.cubicchunks.api;

import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

import javax.annotation.Nullable;

/**
 * Public API for CubicChunks2.
 * Use this to interact with cubic chunks from other mods.
 */
public final class CubicChunksAPI {
    
    private CubicChunksAPI() {}
    
    /**
     * Check if a level is using cubic chunks.
     */
    public static boolean isCubic(LevelHeightAccessor level) {
        return level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic();
    }
    
    /**
     * Get a cube from a level.
     * @return The cube, or null if not loaded or not cubic
     */
    @Nullable
    public static Cube getCube(Level level, CubePos pos) {
        if (level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic()) {
            return cubic.getCube(pos);
        }
        return null;
    }
    
    /**
     * Get or load a cube from a level.
     * May trigger async loading - result may be null initially.
     */
    @Nullable
    public static Cube getOrLoadCube(Level level, CubePos pos) {
        if (level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic()) {
            return cubic.getOrLoadCube(pos);
        }
        return null;
    }
    
    /**
     * Check if a cube is loaded.
     */
    public static boolean isCubeLoaded(Level level, CubePos pos) {
        if (level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic()) {
            return cubic.hasCube(pos);
        }
        return false;
    }
    
    /**
     * Get the minimum build height.
     * In cubic worlds, this is effectively unbounded.
     */
    public static int getMinBuildHeight(LevelHeightAccessor level) {
        return level.getMinBuildHeight();
    }
    
    /**
     * Get the maximum build height.
     * In cubic worlds, this is effectively unbounded.
     */
    public static int getMaxBuildHeight(LevelHeightAccessor level) {
        return level.getMaxBuildHeight();
    }
}
