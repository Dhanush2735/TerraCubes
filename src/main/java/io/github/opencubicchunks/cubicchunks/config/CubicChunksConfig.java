package io.github.opencubicchunks.cubicchunks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CubicChunksConfig {
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.IntValue VERTICAL_VIEW_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAX_GENERATED_CUBES_PER_TICK;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CUBIC_LIGHTING;
    public static final ForgeConfigSpec.BooleanValue FORCE_CUBIC_WORLDS;
    public static final ForgeConfigSpec.IntValue CUBE_UNLOAD_DELAY_SECONDS;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.comment("CubicChunks2 Configuration").push("general");
        
        VERTICAL_VIEW_DISTANCE = builder
            .comment("Vertical view distance in cubes (16 blocks each)")
            .defineInRange("verticalViewDistance", 8, 2, 32);
        
        MAX_GENERATED_CUBES_PER_TICK = builder
            .comment("Maximum cubes to generate per tick (performance tuning)")
            .defineInRange("maxGeneratedCubesPerTick", 16, 1, 256);
        
        ENABLE_CUBIC_LIGHTING = builder
            .comment("Enable cube-based lighting engine (disable for debugging)")
            .define("enableCubicLighting", true);
        
        FORCE_CUBIC_WORLDS = builder
            .comment("Force all worlds to use cubic chunks")
            .define("forceCubicWorlds", false);
        
        CUBE_UNLOAD_DELAY_SECONDS = builder
            .comment("Delay before unloading cubes that are out of range")
            .defineInRange("cubeUnloadDelaySeconds", 15, 1, 300);
        
        builder.pop();
        
        SPEC = builder.build();
    }
}
