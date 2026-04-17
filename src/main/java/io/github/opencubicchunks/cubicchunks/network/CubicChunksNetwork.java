package io.github.opencubicchunks.cubicchunks.network;

import io.github.opencubicchunks.cubicchunks.TerraCubes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Network protocol for cube synchronization.
 */
public class CubicChunksNetwork {
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(TerraCubes.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void register() {
        // Server -> Client: Send cube data
        CHANNEL.registerMessage(
            packetId++,
            PacketCubeData.class,
            PacketCubeData::encode,
            PacketCubeData::decode,
            PacketCubeData::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        
        // Server -> Client: Unload cube
        CHANNEL.registerMessage(
            packetId++,
            PacketUnloadCube.class,
            PacketUnloadCube::encode,
            PacketUnloadCube::decode,
            PacketUnloadCube::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        
        // Server -> Client: Cube block update
        CHANNEL.registerMessage(
            packetId++,
            PacketCubeBlockUpdate.class,
            PacketCubeBlockUpdate::encode,
            PacketCubeBlockUpdate::decode,
            PacketCubeBlockUpdate::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        
        // Server -> Client: Cube light update
        CHANNEL.registerMessage(
            packetId++,
            PacketCubeLightUpdate.class,
            PacketCubeLightUpdate::encode,
            PacketCubeLightUpdate::decode,
            PacketCubeLightUpdate::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        
        TerraCubes.LOGGER.info("Registered {} CubicChunks network packets", packetId);
    }
}
