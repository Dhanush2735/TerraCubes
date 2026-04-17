package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cubicchunks.TerraCubes;
import io.github.opencubicchunks.cubicchunks.network.CubicChunksNetwork;
import io.github.opencubicchunks.cubicchunks.network.PacketCubeData;
import io.github.opencubicchunks.cubicchunks.network.PacketUnloadCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeStatus;
import io.github.opencubicchunks.cubicchunks.world.level.storage.CubeStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cube loading, tracking, and player synchronization on the server.
 */
public class CubeMap {
    private final ServerLevel level;
    private final ConcurrentHashMap<Long, CubeHolder> cubeHolders;
    private final CubeStorage storage;
    private final CubeDistanceManager distanceManager;
    
    public CubeMap(ServerLevel level) {
        this.level = level;
        this.cubeHolders = new ConcurrentHashMap<>();
        
        Path worldPath = level.getServer().getWorldPath(LevelResource.ROOT);
        this.storage = new CubeStorage(worldPath.resolve(level.dimension().location().getPath()), level);
        this.distanceManager = new CubeDistanceManager(this);
    }
    
    @Nullable
    public Cube getCube(CubePos pos) {
        CubeHolder holder = cubeHolders.get(pos.asLong());
        return holder != null ? holder.getCube() : null;
    }
    
    @Nullable
    public Cube getOrLoadCube(CubePos pos) {
        long key = pos.asLong();
        CubeHolder holder = cubeHolders.computeIfAbsent(key, k -> new CubeHolder(pos, this));
        
        if (holder.getCube() == null) {
            // Load from disk or generate
            storage.loadCube(pos).thenAccept(cube -> {
                if (cube != null) {
                    holder.setCube(cube);
                } else {
                    // Generate new cube
                    holder.setCube(generateCube(pos));
                }
            });
        }
        
        return holder.getCube();
    }
    
    public boolean hasCube(CubePos pos) {
        CubeHolder holder = cubeHolders.get(pos.asLong());
        return holder != null && holder.getCube() != null;
    }
    
    private Cube generateCube(CubePos pos) {
        // Basic empty cube generation
        // Real generation is handled by CubicWorldGen mod
        Cube cube = new Cube(level, pos, null);
        cube.setStatus(CubeStatus.EMPTY);
        return cube;
    }
    
    /**
     * Called when a player's position changes to update cube visibility.
     */
    public void updatePlayerCubes(ServerPlayer player) {
        CubePos playerCube = CubePos.fromBlockPos(player.blockPosition());
        int viewDistance = player.server.getPlayerList().getViewDistance();
        int verticalDistance = io.github.opencubicchunks.cubicchunks.config.CubicChunksConfig.VERTICAL_VIEW_DISTANCE.get();
        
        // Send cubes in range
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dy = -verticalDistance; dy <= verticalDistance; dy++) {
                for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                    CubePos cubePos = CubePos.of(
                        playerCube.getX() + dx,
                        playerCube.getY() + dy,
                        playerCube.getZ() + dz
                    );
                    
                    Cube cube = getOrLoadCube(cubePos);
                    if (cube != null && cube.getStatus().isAtLeast(CubeStatus.FULL)) {
                        sendCubeToPlayer(player, cube);
                    }
                }
            }
        }
    }
    
    private void sendCubeToPlayer(ServerPlayer player, Cube cube) {
        CubicChunksNetwork.CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> player),
            new PacketCubeData(cube, true)
        );
    }
    
    /**
     * Save all dirty cubes.
     */
    public void saveAllCubes() {
        for (CubeHolder holder : cubeHolders.values()) {
            Cube cube = holder.getCube();
            if (cube != null && cube.isDirty()) {
                storage.saveCube(cube);
            }
        }
    }
    
    /**
     * Unload cubes that are no longer needed.
     */
    public void tick() {
        distanceManager.tick();
        
        // Unload cubes with no tickets
        cubeHolders.entrySet().removeIf(entry -> {
            CubeHolder holder = entry.getValue();
            if (!holder.hasTickets()) {
                Cube cube = holder.getCube();
                if (cube != null && cube.isDirty()) {
                    storage.saveCube(cube);
                }
                return true;
            }
            return false;
        });
    }
    
    public void close() {
        saveAllCubes();
        storage.close();
    }
    
    public CubeDistanceManager getDistanceManager() {
        return distanceManager;
    }
    
    public ServerLevel getLevel() {
        return level;
    }
}
