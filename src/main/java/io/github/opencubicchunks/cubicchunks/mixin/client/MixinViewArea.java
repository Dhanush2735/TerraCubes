package io.github.opencubicchunks.cubicchunks.mixin.client;

import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ViewArea to use cube-based render sections.
 */
@Mixin(ViewArea.class)
public abstract class MixinViewArea {
    // Cube render section management
}
