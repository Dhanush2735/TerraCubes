package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Packet for sending light updates for a cube.
 */
public class PacketCubeLightUpdate {
    private final CubePos pos;
    @Nullable
    private final byte[] blockLight;
    @Nullable
    private final byte[] skyLight;
    
    public PacketCubeLightUpdate(CubePos pos, @Nullable byte[] blockLight, @Nullable byte[] skyLight) {
        this.pos = pos;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
    }
    
    public static void encode(PacketCubeLightUpdate msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.pos.getX());
        buf.writeInt(msg.pos.getY());
        buf.writeInt(msg.pos.getZ());
        
        buf.writeBoolean(msg.blockLight != null);
        if (msg.blockLight != null) {
            buf.writeByteArray(msg.blockLight);
        }
        
        buf.writeBoolean(msg.skyLight != null);
        if (msg.skyLight != null) {
            buf.writeByteArray(msg.skyLight);
        }
    }
    
    public static PacketCubeLightUpdate decode(FriendlyByteBuf buf) {
        CubePos pos = CubePos.of(buf.readInt(), buf.readInt(), buf.readInt());
        
        byte[] blockLight = buf.readBoolean() ? buf.readByteArray() : null;
        byte[] skyLight = buf.readBoolean() ? buf.readByteArray() : null;
        
        return new PacketCubeLightUpdate(pos, blockLight, skyLight);
    }
    
    public static void handle(PacketCubeLightUpdate msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static void handleClient(PacketCubeLightUpdate msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            // Update cube lighting in client cache
            // Cube cube = ClientCubeCache.get(mc.level).getCube(msg.pos);
            // if (cube != null) {
            //     if (msg.blockLight != null) cube.setBlockLightArray(msg.blockLight);
            //     if (msg.skyLight != null) cube.setSkyLightArray(msg.skyLight);
            // }
        }
    }
}
