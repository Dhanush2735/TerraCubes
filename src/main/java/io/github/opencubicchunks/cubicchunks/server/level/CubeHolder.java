package io.github.opencubicchunks.cubicchunks.server.level;

import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeStatus;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds a cube and tracks its ticket level for loading/unloading.
 */
public class CubeHolder {
    private final CubePos pos;
    private final CubeMap cubeMap;
    private final AtomicReference<Cube> cube;
    private final AtomicInteger ticketCount;
    private volatile CubeStatus targetStatus;
    
    public CubeHolder(CubePos pos, CubeMap cubeMap) {
        this.pos = pos;
        this.cubeMap = cubeMap;
        this.cube = new AtomicReference<>();
        this.ticketCount = new AtomicInteger(0);
        this.targetStatus = CubeStatus.EMPTY;
    }
    
    public CubePos getPos() {
        return pos;
    }
    
    @Nullable
    public Cube getCube() {
        return cube.get();
    }
    
    public void setCube(Cube cube) {
        this.cube.set(cube);
    }
    
    public boolean hasTickets() {
        return ticketCount.get() > 0;
    }
    
    public void addTicket() {
        ticketCount.incrementAndGet();
    }
    
    public void removeTicket() {
        ticketCount.decrementAndGet();
    }
    
    public int getTicketCount() {
        return ticketCount.get();
    }
    
    public CubeStatus getTargetStatus() {
        return targetStatus;
    }
    
    public void setTargetStatus(CubeStatus status) {
        this.targetStatus = status;
    }
}
