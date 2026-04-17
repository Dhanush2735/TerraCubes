package io.github.opencubicchunks.cubicchunks.mixin.server;

import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ChunkHolder to support cube lifecycle tracking.
 */
@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {
    // CubeHolder will be a separate class
}
