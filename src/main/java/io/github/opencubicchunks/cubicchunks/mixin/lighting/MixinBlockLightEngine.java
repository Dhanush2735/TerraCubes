package io.github.opencubicchunks.cubicchunks.mixin.lighting;

import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockLightEngine.class)
public abstract class MixinBlockLightEngine {
    // Block light propagation for cubic worlds
}
