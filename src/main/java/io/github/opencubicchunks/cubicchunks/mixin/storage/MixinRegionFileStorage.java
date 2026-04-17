package io.github.opencubicchunks.cubicchunks.mixin.storage;

import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for RegionFileStorage.
 * Cubes use a separate 3D region format.
 */
@Mixin(RegionFileStorage.class)
public abstract class MixinRegionFileStorage {
    // 3D region files handled by CubeStorage.CubeRegionFile
}
