package io.github.opencubicchunks.cubicchunks.mixin.client;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ClientChunkCache to integrate with cube caching.
 */
@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache {
    // Client cube caching handled by ClientCubeCache
}
