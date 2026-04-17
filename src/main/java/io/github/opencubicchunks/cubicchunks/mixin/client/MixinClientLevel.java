package io.github.opencubicchunks.cubicchunks.mixin.client;

import io.github.opencubicchunks.cubicchunks.client.ClientCubeCache;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Mixin to add cube support to ClientLevel.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level implements CubicLevelHeightAccessor {
    
    @Unique
    private boolean cc$cubic = false;
    
    @Unique
    private ClientCubeCache cc$cubeCache;
    
    protected MixinClientLevel() {
        super(null, null, null, null, null, false, false, 0, 0);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void cc$init(CallbackInfo ci) {
        this.cc$cubeCache = new ClientCubeCache((ClientLevel) (Object) this);
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
        return cc$cubic ? cc$cubeCache.getCube(pos) : null;
    }
    
    @Override
    @Nullable
    public Cube getOrLoadCube(CubePos pos) {
        return getCube(pos); // Client doesn't load, waits for server
    }
    
    @Override
    public boolean hasCube(CubePos pos) {
        return cc$cubic && cc$cubeCache.hasCube(pos);
    }
    
    @Unique
    public ClientCubeCache getCubeCache() {
        return cc$cubeCache;
    }
}
