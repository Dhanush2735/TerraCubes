package io.github.opencubicchunks.cubicchunks.mixin.network;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for ServerGamePacketListenerImpl to handle cube-related packets.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListener {
    // Cube packet handling registered through CubicChunksNetwork
}
