package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for sending block updates within a cube to clients.
 */
public class PacketCubeBlockUpdate {
    private final CubePos cubePos;
    private final short localPos; // Packed local position (4 bits each for x, y, z)
    private final BlockState state;
    
    public PacketCubeBlockUpdate(CubePos cubePos, BlockPos blockPos, BlockState state) {
        this.cubePos = cubePos;
        this.localPos = packLocalPos(
            blockPos.getX() & 15,
            blockPos.getY() & 15,
            blockPos.getZ() & 15
        );
        this.state = state;
    }
    
    private PacketCubeBlockUpdate(CubePos cubePos, short localPos, BlockState state) {
        this.cubePos = cubePos;
        this.localPos = localPos;
        this.state = state;
    }
    
    private static short packLocalPos(int x, int y, int z) {
        return (short) ((x & 15) | ((y & 15) << 4) | ((z & 15) << 8));
    }
    
    private BlockPos unpackToBlockPos() {
        int x = localPos & 15;
        int y = (localPos >> 4) & 15;
        int z = (localPos >> 8) & 15;
        return new BlockPos(
            cubePos.minBlockX() + x,
            cubePos.minBlockY() + y,
            cubePos.minBlockZ() + z
        );
    }
    
    public static void encode(PacketCubeBlockUpdate msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cubePos.getX());
        buf.writeInt(msg.cubePos.getY());
        buf.writeInt(msg.cubePos.getZ());
        buf.writeShort(msg.localPos);
        buf.writeVarInt(Block.getId(msg.state));
    }
    
    public static PacketCubeBlockUpdate decode(FriendlyByteBuf buf) {
        CubePos cubePos = CubePos.of(buf.readInt(), buf.readInt(), buf.readInt());
        short localPos = buf.readShort();
        BlockState state = Block.stateById(buf.readVarInt());
        return new PacketCubeBlockUpdate(cubePos, localPos, state);
    }
    
    public static void handle(PacketCubeBlockUpdate msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static void handleClient(PacketCubeBlockUpdate msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            BlockPos pos = msg.unpackToBlockPos();
            mc.level.setBlock(pos, msg.state, Block.UPDATE_ALL);
        }
    }
}
