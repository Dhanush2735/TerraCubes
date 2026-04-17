package io.github.opencubicchunks.cubicchunks.world.level.storage;

import io.github.opencubicchunks.cubicchunks.TerraCubes;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles cube persistence using a 3D region file format.
 * Region files are organized as 32x32x32 cubes per region.
 */
public class CubeStorage implements AutoCloseable {
    private static final int REGION_SIZE = 32;
    private static final int REGION_SIZE_BITS = 5;
    
    private final Path storagePath;
    private final Level level;
    private final ConcurrentHashMap<Long, CubeRegionFile> regionCache;
    private final ExecutorService ioExecutor;
    
    public CubeStorage(Path storagePath, Level level) {
        this.storagePath = storagePath.resolve("cubes");
        this.level = level;
        this.regionCache = new ConcurrentHashMap<>();
        this.ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "CubeStorage-IO");
            t.setDaemon(true);
            return t;
        });
        
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            TerraCubes.LOGGER.error("Failed to create cube storage directory", e);
        }
    }
    
    /**
     * Load a cube from disk.
     */
    @Nullable
    public CompletableFuture<Cube> loadCube(CubePos pos) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CubeRegionFile region = getRegionFile(pos);
                CompoundTag tag = region.readCube(pos);
                if (tag == null) {
                    return null;
                }
                return Cube.deserialize(level, tag);
            } catch (IOException e) {
                TerraCubes.LOGGER.error("Failed to load cube at {}", pos, e);
                return null;
            }
        }, ioExecutor);
    }
    
    /**
     * Save a cube to disk.
     */
    public CompletableFuture<Void> saveCube(Cube cube) {
        CompoundTag tag = cube.serialize();
        CubePos pos = cube.getPos();
        
        return CompletableFuture.runAsync(() -> {
            try {
                CubeRegionFile region = getRegionFile(pos);
                region.writeCube(pos, tag);
                cube.setDirty(false);
            } catch (IOException e) {
                TerraCubes.LOGGER.error("Failed to save cube at {}", pos, e);
            }
        }, ioExecutor);
    }
    
    private CubeRegionFile getRegionFile(CubePos cubePos) throws IOException {
        int regionX = cubePos.getX() >> REGION_SIZE_BITS;
        int regionY = cubePos.getY() >> REGION_SIZE_BITS;
        int regionZ = cubePos.getZ() >> REGION_SIZE_BITS;
        long regionKey = packRegionKey(regionX, regionY, regionZ);
        
        return regionCache.computeIfAbsent(regionKey, k -> {
            Path regionPath = storagePath.resolve(
                String.format("r.%d.%d.%d.cc3", regionX, regionY, regionZ)
            );
            try {
                return new CubeRegionFile(regionPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
    
    private static long packRegionKey(int x, int y, int z) {
        return ((long) x & 0x1FFFFF) << 42 
             | ((long) y & 0x1FFFFF) << 21 
             | ((long) z & 0x1FFFFF);
    }
    
    /**
     * Flush all pending writes and close region files.
     */
    @Override
    public void close() {
        ioExecutor.shutdown();
        for (CubeRegionFile region : regionCache.values()) {
            try {
                region.close();
            } catch (IOException e) {
                TerraCubes.LOGGER.error("Failed to close region file", e);
            }
        }
        regionCache.clear();
    }
    
    /**
     * Simple 3D region file implementation.
     */
    public static class CubeRegionFile implements AutoCloseable {
        private static final int CUBES_PER_REGION = REGION_SIZE * REGION_SIZE * REGION_SIZE;
        
        private final Path path;
        private final RandomAccessFile file;
        private final int[] offsets;
        private final Object lock = new Object();
        
        public CubeRegionFile(Path path) throws IOException {
            this.path = path;
            this.offsets = new int[CUBES_PER_REGION];
            
            boolean exists = Files.exists(path);
            this.file = new RandomAccessFile(path.toFile(), "rw");
            
            if (exists && file.length() >= CUBES_PER_REGION * 4) {
                // Read offset table
                file.seek(0);
                for (int i = 0; i < CUBES_PER_REGION; i++) {
                    offsets[i] = file.readInt();
                }
            } else {
                // Initialize empty offset table
                file.seek(0);
                for (int i = 0; i < CUBES_PER_REGION; i++) {
                    file.writeInt(0);
                }
            }
        }
        
        private int getCubeIndex(CubePos pos) {
            int localX = pos.getX() & (REGION_SIZE - 1);
            int localY = pos.getY() & (REGION_SIZE - 1);
            int localZ = pos.getZ() & (REGION_SIZE - 1);
            return (localY * REGION_SIZE * REGION_SIZE) + (localZ * REGION_SIZE) + localX;
        }
        
        @Nullable
        public CompoundTag readCube(CubePos pos) throws IOException {
            synchronized (lock) {
                int index = getCubeIndex(pos);
                int offset = offsets[index];
                
                if (offset == 0) {
                    return null;
                }
                
                file.seek(offset);
                int length = file.readInt();
                byte[] data = new byte[length];
                file.readFully(data);
                
                return NbtIo.readCompressed(new ByteArrayInputStream(data));
            }
        }
        
        public void writeCube(CubePos pos, CompoundTag tag) throws IOException {
            synchronized (lock) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, baos);
                byte[] data = baos.toByteArray();
                
                int index = getCubeIndex(pos);
                
                // Append to end of file
                long newOffset = file.length();
                file.seek(newOffset);
                file.writeInt(data.length);
                file.write(data);
                
                // Update offset table
                offsets[index] = (int) newOffset;
                file.seek(index * 4L);
                file.writeInt((int) newOffset);
            }
        }
        
        @Override
        public void close() throws IOException {
            file.close();
        }
    }
}
