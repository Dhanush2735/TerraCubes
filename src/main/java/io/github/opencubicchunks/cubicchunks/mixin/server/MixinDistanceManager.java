package io.github.opencubicchunks.cubicchunks.mixin.server;

import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for DistanceManager to add cube ticket support.
 */
@Mixin(DistanceManager.class)
public abstract class MixinDistanceManager {
    // Cube distance management handled by CubeDistanceManager
}
