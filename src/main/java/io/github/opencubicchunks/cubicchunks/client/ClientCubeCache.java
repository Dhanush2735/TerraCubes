package io.github.opencubicchunks.cubicchunks.client;

import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.client.multiplayer.ClientLevel;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache for cubes received from the server.
 */
public class ClientCubeCache {
    private final ClientLevel level;
    private final ConcurrentHashMap<Long, Cube> cubes;
    
    public ClientCubeCache(ClientLevel level) {
        this.level = level;
        this.cubes = new ConcurrentHashMap<>();
    }
    
    @Nullable
    public Cube getCube(CubePos pos) {
        return cubes.get(pos.asLong());
    }
    
    public boolean hasCube(CubePos pos) {
        return cubes.containsKey(pos.asLong());
    }
    
    public void setCube(CubePos pos, Cube cube) {
        cubes.put(pos.asLong(), cube);
    }
    
    public void unloadCube(CubePos pos) {
        cubes.remove(pos.asLong());
    }
    
    public void clear() {
        cubes.clear();
    }
    
    public int getCubeCount() {
        return cubes.size();
    }
}
