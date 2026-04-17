package io.github.opencubicchunks.cubicchunks.mixin.server;

import io.github.opencubicchunks.cubicchunks.server.level.CubeMap;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Mixin to add cube management to ServerLevel.
 */
@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements CubicLevelHeightAccessor {
    
    @Unique
    private boolean cc$cubic = false;
    
    @Unique
    private CubeMap cc$cubeMap;
    
    protected MixinServerLevel() {
        super(null, null, null, null, null, false, false, 0, 0);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void cc$init(CallbackInfo ci) {
        // Initialize cube map - will be enabled by world type
        this.cc$cubeMap = new CubeMap((ServerLevel) (Object) this);
    }
    
    @Override
    public boolean isCubic() {
        return cc$cubic;
    }
    
    @Override
    public void setCubic(boolean cubic) {
        this.cc$cubic = cubic;
    }
    
    @Override
    @Nullable
    public Cube getCube(CubePos pos) {
        return cc$cubic ? cc$cubeMap.getCube(pos) : null;
    }
    
    @Override
    @Nullable
    public Cube getOrLoadCube(CubePos pos) {
        return cc$cubic ? cc$cubeMap.getOrLoadCube(pos) : null;
    }
    
    @Override
    public boolean hasCube(CubePos pos) {
        return cc$cubic && cc$cubeMap.hasCube(pos);
    }
    
    @Unique
    public CubeMap getCubeMap() {
        return cc$cubeMap;
    }
}
