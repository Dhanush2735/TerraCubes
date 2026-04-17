package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cube loading tickets based on player proximity.
 */
public class CubeDistanceManager {
    private final CubeMap cubeMap;
    private final Map<UUID, Set<Long>> playerCubes;
    
    public CubeDistanceManager(CubeMap cubeMap) {
        this.cubeMap = cubeMap;
        this.playerCubes = new ConcurrentHashMap<>();
    }
    
    /**
     * Update tickets for a player based on their current position.
     */
    public void updatePlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        CubePos playerCube = CubePos.fromBlockPos(player.blockPosition());
        
        int viewDistance = player.server.getPlayerList().getViewDistance();
        int verticalDistance = io.github.opencubicchunks.cubicchunks.config.CubicChunksConfig.VERTICAL_VIEW_DISTANCE.get();
        
        Set<Long> newCubes = new HashSet<>();
        
        // Calculate cubes in range
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dy = -verticalDistance; dy <= verticalDistance; dy++) {
                for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                    // Sphere check for better performance
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist <= viewDistance) {
                        CubePos cubePos = CubePos.of(
                            playerCube.getX() + dx,
                            playerCube.getY() + dy,
                            playerCube.getZ() + dz
                        );
                        newCubes.add(cubePos.asLong());
                    }
                }
            }
        }
        
        Set<Long> oldCubes = playerCubes.getOrDefault(playerId, Collections.emptySet());
        
        // Remove tickets for cubes no longer in range
        for (long cubeKey : oldCubes) {
            if (!newCubes.contains(cubeKey)) {
                CubeHolder holder = getHolder(CubePos.fromLong(cubeKey));
                if (holder != null) {
                    holder.removeTicket();
                }
            }
        }
        
        // Add tickets for new cubes
        for (long cubeKey : newCubes) {
            if (!oldCubes.contains(cubeKey)) {
                CubePos pos = CubePos.fromLong(cubeKey);
                cubeMap.getOrLoadCube(pos); // Ensure loaded
                CubeHolder holder = getHolder(pos);
                if (holder != null) {
                    holder.addTicket();
                }
            }
        }
        
        playerCubes.put(playerId, newCubes);
    }
    
    /**
     * Remove all tickets for a player (on disconnect).
     */
    public void removePlayer(UUID playerId) {
        Set<Long> cubes = playerCubes.remove(playerId);
        if (cubes != null) {
            for (long cubeKey : cubes) {
                CubeHolder holder = getHolder(CubePos.fromLong(cubeKey));
                if (holder != null) {
                    holder.removeTicket();
                }
            }
        }
    }
    
    private CubeHolder getHolder(CubePos pos) {
        // Access internal holder map - would need accessor
        return null; // Placeholder
    }
    
    public void tick() {
        // Periodic ticket cleanup
    }
}
