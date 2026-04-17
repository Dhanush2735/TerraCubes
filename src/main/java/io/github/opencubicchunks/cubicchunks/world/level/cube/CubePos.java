package io.github.opencubicchunks.cubicchunks.world.level.cube;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents the position of a 16x16x16 cube in the world.
 * This is the cubic equivalent of ChunkPos.
 */
public final class CubePos {
    public static final int CUBE_SIZE = 16;
    public static final int CUBE_SIZE_BITS = 4;
    
    public static final Codec<CubePos> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("x").forGetter(CubePos::getX),
            Codec.INT.fieldOf("y").forGetter(CubePos::getY),
            Codec.INT.fieldOf("z").forGetter(CubePos::getZ)
        ).apply(instance, CubePos::new)
    );
    
    private final int x;
    private final int y;
    private final int z;
    
    public CubePos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static CubePos of(int x, int y, int z) {
        return new CubePos(x, y, z);
    }
    
    public static CubePos fromBlockPos(BlockPos pos) {
        return new CubePos(
            pos.getX() >> CUBE_SIZE_BITS,
            pos.getY() >> CUBE_SIZE_BITS,
            pos.getZ() >> CUBE_SIZE_BITS
        );
    }
    
    public static CubePos fromSectionPos(SectionPos sectionPos) {
        return new CubePos(sectionPos.x(), sectionPos.y(), sectionPos.z());
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    
    public int minBlockX() { return x << CUBE_SIZE_BITS; }
    public int minBlockY() { return y << CUBE_SIZE_BITS; }
    public int minBlockZ() { return z << CUBE_SIZE_BITS; }
    
    public int maxBlockX() { return minBlockX() + CUBE_SIZE - 1; }
    public int maxBlockY() { return minBlockY() + CUBE_SIZE - 1; }
    public int maxBlockZ() { return minBlockZ() + CUBE_SIZE - 1; }
    
    public BlockPos minBlockPos() {
        return new BlockPos(minBlockX(), minBlockY(), minBlockZ());
    }
    
    public BlockPos centerBlockPos() {
        return new BlockPos(
            minBlockX() + CUBE_SIZE / 2,
            minBlockY() + CUBE_SIZE / 2,
            minBlockZ() + CUBE_SIZE / 2
        );
    }
    
    /**
     * Get the column (ChunkPos) this cube is in.
     */
    public ChunkPos asChunkPos() {
        return new ChunkPos(x, z);
    }
    
    /**
     * Get the SectionPos for this cube position.
     */
    public SectionPos asSectionPos() {
        return SectionPos.of(x, y, z);
    }
    
    /**
     * Pack cube position into a long for efficient storage/hashing.
     * Format: [x: 21 bits][y: 22 bits][z: 21 bits]
     */
    public long asLong() {
        return asLong(x, y, z);
    }
    
    public static long asLong(int x, int y, int z) {
        long result = 0L;
        result |= ((long) x & 0x1FFFFF) << 43;
        result |= ((long) y & 0x3FFFFF) << 21;
        result |= ((long) z & 0x1FFFFF);
        return result;
    }
    
    public static CubePos fromLong(long packed) {
        int x = (int) (packed >> 43);
        int y = (int) (packed >> 21 & 0x3FFFFF);
        int z = (int) (packed & 0x1FFFFF);
        // Sign extension for negative values
        if ((x & 0x100000) != 0) x |= 0xFFE00000;
        if ((y & 0x200000) != 0) y |= 0xFFC00000;
        if ((z & 0x100000) != 0) z |= 0xFFE00000;
        return new CubePos(x, y, z);
    }
    
    /**
     * Stream all block positions within this cube.
     */
    public Stream<BlockPos> blockPositions() {
        return BlockPos.betweenClosedStream(
            minBlockX(), minBlockY(), minBlockZ(),
            maxBlockX(), maxBlockY(), maxBlockZ()
        );
    }
    
    /**
     * Get adjacent cube positions (6 neighbors).
     */
    public CubePos[] getNeighbors() {
        return new CubePos[] {
            new CubePos(x - 1, y, z),
            new CubePos(x + 1, y, z),
            new CubePos(x, y - 1, z),
            new CubePos(x, y + 1, z),
            new CubePos(x, y, z - 1),
            new CubePos(x, y, z + 1)
        };
    }
    
    /**
     * Get all 26 neighbors (including diagonals).
     */
    public Stream<CubePos> allNeighbors() {
        return Stream.of(
            new int[][] {
                {-1,-1,-1}, {-1,-1,0}, {-1,-1,1},
                {-1,0,-1}, {-1,0,0}, {-1,0,1},
                {-1,1,-1}, {-1,1,0}, {-1,1,1},
                {0,-1,-1}, {0,-1,0}, {0,-1,1},
                {0,0,-1}, {0,0,1},
                {0,1,-1}, {0,1,0}, {0,1,1},
                {1,-1,-1}, {1,-1,0}, {1,-1,1},
                {1,0,-1}, {1,0,0}, {1,0,1},
                {1,1,-1}, {1,1,0}, {1,1,1}
            }
        ).map(d -> new CubePos(x + d[0], y + d[1], z + d[2]));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CubePos cubePos)) return false;
        return x == cubePos.x && y == cubePos.y && z == cubePos.z;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
    
    @Override
    public String toString() {
        return "CubePos[" + x + ", " + y + ", " + z + "]";
    }
}
