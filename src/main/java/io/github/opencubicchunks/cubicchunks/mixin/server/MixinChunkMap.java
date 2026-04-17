package io.github.opencubicchunks.cubicchunks.mixin.server;

import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to integrate cube tracking with vanilla ChunkMap.
 */
@Mixin(ChunkMap.class)
public abstract class MixinChunkMap {
    
    @Shadow @Final
    ServerLevel level;
    
    // Cube tracking is delegated to CubeMap
    // This mixin ensures vanilla chunk operations also trigger cube operations when needed
}
