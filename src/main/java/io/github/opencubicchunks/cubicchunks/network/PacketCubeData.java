package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Packet containing full cube data for client synchronization.
 * Sent when a player enters range of a cube.
 */
public class PacketCubeData {
    private final CubePos pos;
    private final byte[] data;
    private final boolean fullUpdate;
    
    public PacketCubeData(Cube cube, boolean fullUpdate) {
        this.pos = cube.getPos();
        this.fullUpdate = fullUpdate;
        
        CompoundTag tag = cube.serialize();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(tag, baos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize cube", e);
        }
        this.data = baos.toByteArray();
    }
    
    private PacketCubeData(CubePos pos, byte[] data, boolean fullUpdate) {
        this.pos = pos;
        this.data = data;
        this.fullUpdate = fullUpdate;
    }
    
    public static void encode(PacketCubeData msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.pos.getX());
        buf.writeInt(msg.pos.getY());
        buf.writeInt(msg.pos.getZ());
        buf.writeBoolean(msg.fullUpdate);
        buf.writeByteArray(msg.data);
    }
    
    public static PacketCubeData decode(FriendlyByteBuf buf) {
        CubePos pos = CubePos.of(buf.readInt(), buf.readInt(), buf.readInt());
        boolean fullUpdate = buf.readBoolean();
        byte[] data = buf.readByteArray();
        return new PacketCubeData(pos, data, fullUpdate);
    }
    
    public static void handle(PacketCubeData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static void handleClient(PacketCubeData msg) {
        try {
            CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(msg.data));
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Cube cube = Cube.deserialize(mc.level, tag);
                // Store cube in client-side cube cache
                // ClientCubeCache.get(mc.level).setCube(msg.pos, cube);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize cube", e);
        }
    }
}
