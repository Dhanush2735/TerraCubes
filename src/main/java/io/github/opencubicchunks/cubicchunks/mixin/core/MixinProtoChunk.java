package io.github.opencubicchunks.cubicchunks.mixin.core;

import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ProtoChunk to support cubic worldgen.
 */
@Mixin(ProtoChunk.class)
public abstract class MixinProtoChunk {
    // Cubic worldgen integration will be handled by CubicWorldGen mod
}
