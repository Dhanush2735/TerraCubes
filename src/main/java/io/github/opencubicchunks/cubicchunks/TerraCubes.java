package io.github.opencubicchunks.cubicchunks;

import io.github.opencubicchunks.cubicchunks.config.CubicChunksConfig;
import io.github.opencubicchunks.cubicchunks.network.CubicChunksNetwork;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TerraCubes.MODID)
public class TerraCubes {
    public static final String MODID = "terracubes";
    public static final Logger LOGGER = LoggerFactory.getLogger(TerraCubes.class);

    public TerraCubes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CubicChunksConfig.SPEC);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("TerraCubes initializing - Infinite vertical worlds enabled");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CubicChunksNetwork.register();
            LOGGER.info("TerraCubes network protocol registered");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TerraCubes server starting - Cube storage engine active");
    }

    public static boolean isCubic(net.minecraft.world.level.LevelHeightAccessor level) {
        return level instanceof CubicLevelHeightAccessor cubic && cubic.isCubic();
    }
}