package io.github.opencubicchunks.cubicchunks.mixin.core;

import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.world.level.cube.Cube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubePos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {
    
    // Use BOTH Mojang and SRG names for production compatibility
    @Inject(method = {"getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", 
                      "m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"}, 
            at = @At("HEAD"), 
            cancellable = true,
            require = 1)  // Only require one to match
    private void cc$getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        LevelChunk self = (LevelChunk)(Object) this;
        Level level = self.getLevel();
        
        if (level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic()) {
            CubePos cubePos = CubePos.fromBlockPos(pos);
            Cube cube = cubic.getCube(cubePos);
            if (cube != null) {
                cir.setReturnValue(cube.getBlockState(pos));
            }
        }
    }
}