package io.github.opencubicchunks.cubicchunks.mixin.core;

import io.github.opencubicchunks.cubicchunks.world.level.chunk.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mixin to add cube storage to ChunkAccess.
 * This allows chunks (columns) to reference their constituent cubes.
 */
@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess implements CubeAccess {
    
    @Unique
    private final ConcurrentHashMap<Integer, Cube> cc$cubes = new ConcurrentHashMap<>();
    
    @Override
    @Nullable
    public Cube getCube(int cubeY) {
        return cc$cubes.get(cubeY);
    }
    
    @Override
    public void setCube(int cubeY, Cube cube) {
        cc$cubes.put(cubeY, cube);
    }
    
    @Override
    public void removeCube(int cubeY) {
        cc$cubes.remove(cubeY);
    }
    
    @Override
    public Iterable<Cube> getCubes() {
        return cc$cubes.values();
    }
    
    @Override
    public boolean hasCubes() {
        return !cc$cubes.isEmpty();
    }
}
