package io.github.opencubicchunks.cubicchunks.mixin.storage;

import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ChunkStorage to handle cube-based persistence.
 */
@Mixin(ChunkStorage.class)
public abstract class MixinChunkStorage {
    // Cube storage handled by separate CubeStorage class
    // This mixin ensures vanilla chunk saving doesn't interfere
}
