package io.github.opencubicchunks.cubicchunks.world.level.cube;

/**
 * Generation status for cubes, similar to ChunkStatus but for 3D chunks.
 */
public enum CubeStatus {
    EMPTY(0),
    STRUCTURE_STARTS(1),
    STRUCTURE_REFERENCES(2),
    BIOMES(3),
    NOISE(4),
    SURFACE(5),
    CARVERS(6),
    FEATURES(7),
    LIGHT(8),
    SPAWN(9),
    FULL(10);
    
    private final int index;
    
    CubeStatus(int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }
    
    public boolean isAtLeast(CubeStatus status) {
        return this.index >= status.index;
    }
    
    public boolean isBefore(CubeStatus status) {
        return this.index < status.index;
    }
    
    public CubeStatus getParent() {
        return this.index > 0 ? values()[this.index - 1] : this;
    }
    
    public static CubeStatus fromIndex(int index) {
        CubeStatus[] values = values();
        return index >= 0 && index < values.length ? values[index] : EMPTY;
    }
}
