package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to tell the client to unload a cube.
 * Sent when a player moves out of range.
 */
public class PacketUnloadCube {
    private final CubePos pos;
    
    public PacketUnloadCube(CubePos pos) {
        this.pos = pos;
    }
    
    public static void encode(PacketUnloadCube msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.pos.getX());
        buf.writeInt(msg.pos.getY());
        buf.writeInt(msg.pos.getZ());
    }
    
    public static PacketUnloadCube decode(FriendlyByteBuf buf) {
        return new PacketUnloadCube(CubePos.of(buf.readInt(), buf.readInt(), buf.readInt()));
    }
    
    public static void handle(PacketUnloadCube msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static void handleClient(PacketUnloadCube msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            // Remove cube from client-side cache
            // ClientCubeCache.get(mc.level).unloadCube(msg.pos);
        }
    }
}
