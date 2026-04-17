package io.github.opencubicchunks.cubicchunks.world.level.cube;

import com.mojang.serialization.Codec;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelHeightAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.ticks.LevelChunkTicks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A 16x16x16 cube - the fundamental storage unit in CubicChunks.
 * This replaces the column-based chunk for vertical storage.
 */
public class Cube {
    public static final int SIZE = 16;
    public static final int SIZE_BITS = 4;
    public static final int BLOCK_COUNT = SIZE * SIZE * SIZE; // 4096
    
    private final CubePos pos;
    private final Level level;
    private final LevelChunkSection section;
    private final Map<BlockPos, BlockEntity> blockEntities;
    private final Set<Entity> entities;
    private final LevelChunkTicks<Block> blockTicks;
    private final LevelChunkTicks<net.minecraft.world.level.material.Fluid> fluidTicks;
    
    private volatile CubeStatus status;
    private volatile boolean dirty;
    private volatile boolean lightCorrect;
    private long inhabitedTime;
    
    // Lighting data
    private byte[] blockLight;
    private byte[] skyLight;
    
    public Cube(Level level, CubePos pos, LevelChunkSection section) {
        this.level = level;
        this.pos = pos;
        this.section = section != null ? section : createEmptySection(level);
        this.blockEntities = new ConcurrentHashMap<>();
        this.entities = ConcurrentHashMap.newKeySet();
        this.blockTicks = new LevelChunkTicks<>();
        this.fluidTicks = new LevelChunkTicks<>();
        this.status = CubeStatus.EMPTY;
        this.dirty = false;
        this.lightCorrect = false;
        this.inhabitedTime = 0;
        this.blockLight = new byte[2048]; // 4 bits per block, packed
        this.skyLight = new byte[2048];
    }
    
    private static LevelChunkSection createEmptySection(Level level) {
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        return new LevelChunkSection(biomeRegistry);
    }
    
    public CubePos getPos() { return pos; }
    public Level getLevel() { return level; }
    public CubeStatus getStatus() { return status; }
    public void setStatus(CubeStatus status) { this.status = status; }
    
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    
    public boolean isLightCorrect() { return lightCorrect; }
    public void setLightCorrect(boolean lightCorrect) { this.lightCorrect = lightCorrect; }
    
    public long getInhabitedTime() { return inhabitedTime; }
    public void incrementInhabitedTime(long amount) { this.inhabitedTime += amount; }
    
    // ===== BLOCK ACCESS =====
    
    public BlockState getBlockState(int x, int y, int z) {
        return section.getBlockState(x & 15, y & 15, z & 15);
    }
    
    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }
    
    @Nullable
    public BlockState setBlockState(int x, int y, int z, BlockState state, boolean moved) {
        int localX = x & 15;
        int localY = y & 15;
        int localZ = z & 15;
        
        BlockState oldState = section.setBlockState(localX, localY, localZ, state, true);
        
        if (oldState == state) {
            return null;
        }
        
        this.dirty = true;
        
        BlockPos blockPos = new BlockPos(
            pos.minBlockX() + localX,
            pos.minBlockY() + localY,
            pos.minBlockZ() + localZ
        );
        
        // Handle block entity changes
        if (oldState.hasBlockEntity()) {
            BlockEntity oldBe = blockEntities.remove(blockPos);
            if (oldBe != null) {
                oldBe.setRemoved();
            }
        }
        
        if (state.hasBlockEntity()) {
            BlockEntity newBe = ((net.minecraft.world.level.block.EntityBlock) state.getBlock())
                .newBlockEntity(blockPos, state);
            if (newBe != null) {
                blockEntities.put(blockPos, newBe);
                newBe.setLevel(level);
            }
        }
        
        return oldState;
    }
    
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
        return setBlockState(pos.getX(), pos.getY(), pos.getZ(), state, moved);
    }
    
    // ===== BLOCK ENTITIES =====
    
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return blockEntities.get(pos);
    }
    
    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        blockEntity.setLevel(level);
        blockEntities.put(pos, blockEntity);
        this.dirty = true;
    }
    
    public void removeBlockEntity(BlockPos pos) {
        BlockEntity be = blockEntities.remove(pos);
        if (be != null) {
            be.setRemoved();
            this.dirty = true;
        }
    }
    
    public Collection<BlockEntity> getBlockEntities() {
        return Collections.unmodifiableCollection(blockEntities.values());
    }
    
    // ===== ENTITIES =====
    
    public void addEntity(Entity entity) {
        entities.add(entity);
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    public Set<Entity> getEntities() {
        return Collections.unmodifiableSet(entities);
    }
    
    // ===== TICKS =====
    
    public LevelChunkTicks<Block> getBlockTicks() { return blockTicks; }
    public LevelChunkTicks<net.minecraft.world.level.material.Fluid> getFluidTicks() { return fluidTicks; }
    
    // ===== LIGHTING =====
    
    public int getBlockLight(int x, int y, int z) {
        int index = getPackedLightIndex(x & 15, y & 15, z & 15);
        int byteIndex = index >> 1;
        return (index & 1) == 0 
            ? (blockLight[byteIndex] & 0x0F) 
            : ((blockLight[byteIndex] >> 4) & 0x0F);
    }
    
    public void setBlockLight(int x, int y, int z, int light) {
        int index = getPackedLightIndex(x & 15, y & 15, z & 15);
        int byteIndex = index >> 1;
        if ((index & 1) == 0) {
            blockLight[byteIndex] = (byte) ((blockLight[byteIndex] & 0xF0) | (light & 0x0F));
        } else {
            blockLight[byteIndex] = (byte) ((blockLight[byteIndex] & 0x0F) | ((light & 0x0F) << 4));
        }
        this.dirty = true;
    }
    
    public int getSkyLight(int x, int y, int z) {
        int index = getPackedLightIndex(x & 15, y & 15, z & 15);
        int byteIndex = index >> 1;
        return (index & 1) == 0 
            ? (skyLight[byteIndex] & 0x0F) 
            : ((skyLight[byteIndex] >> 4) & 0x0F);
    }
    
    public void setSkyLight(int x, int y, int z, int light) {
        int index = getPackedLightIndex(x & 15, y & 15, z & 15);
        int byteIndex = index >> 1;
        if ((index & 1) == 0) {
            skyLight[byteIndex] = (byte) ((skyLight[byteIndex] & 0xF0) | (light & 0x0F));
        } else {
            skyLight[byteIndex] = (byte) ((skyLight[byteIndex] & 0x0F) | ((light & 0x0F) << 4));
        }
        this.dirty = true;
    }
    
    private static int getPackedLightIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }
    
    public byte[] getBlockLightArray() { return blockLight; }
    public byte[] getSkyLightArray() { return skyLight; }
    public void setBlockLightArray(byte[] data) { this.blockLight = data; }
    public void setSkyLightArray(byte[] data) { this.skyLight = data; }
    
    // ===== BIOMES =====
    
    public net.minecraft.core.Holder<Biome> getBiome(int x, int y, int z) {
        return section.getNoiseBiome(x >> 2, y >> 2, z >> 2);
    }
    
    // ===== SECTION ACCESS =====
    
    public LevelChunkSection getSection() { return section; }
    
    public boolean isEmpty() {
        return section.hasOnlyAir();
    }
    
    // ===== SERIALIZATION =====
    
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("Pos", pos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
            .getOrThrow(false, s -> {}));
        tag.putString("Status", status.name());
        tag.putLong("InhabitedTime", inhabitedTime);
        tag.putBoolean("LightCorrect", lightCorrect);
        tag.putByteArray("BlockLight", blockLight);
        tag.putByteArray("SkyLight", skyLight);
        
        // Serialize section using Codecs (1.20.1 way)
if (!section.hasOnlyAir()) {
    Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
    
    // Block state codec
    Codec<PalettedContainer<BlockState>> blockStateCodec = PalettedContainer.codecRW(
        Block.BLOCK_STATE_REGISTRY,
        BlockState.CODEC,
        PalettedContainer.Strategy.SECTION_STATES,
        Blocks.AIR.defaultBlockState()
    );
    
    // Biome codec
    Codec<PalettedContainer<Holder<Biome>>> biomeCodec = PalettedContainer.codecRW(
        biomeRegistry.asHolderIdMap(),
        biomeRegistry.holderByNameCodec(),
        PalettedContainer.Strategy.SECTION_BIOMES,
        biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
    );
    
    // Encode block states
    blockStateCodec.encodeStart(NbtOps.INSTANCE, section.getStates())
        .result().ifPresent(nbt -> tag.put("block_states", nbt));
    
    // Encode biomes - cast RO to full container
    @SuppressWarnings("unchecked")
    PalettedContainer<Holder<Biome>> biomeContainer = 
        (PalettedContainer<Holder<Biome>>) section.getBiomes();
    
    biomeCodec.encodeStart(NbtOps.INSTANCE, biomeContainer)
        .result().ifPresent(nbt -> tag.put("biomes", nbt));
}
        
        // Serialize block entities
        ListTag beList = new ListTag();
        for (BlockEntity be : blockEntities.values()) {
            beList.add(be.saveWithFullMetadata());
        }
        tag.put("BlockEntities", beList);
        
        return tag;
    }
    
    public static Cube deserialize(Level level, CompoundTag tag) {
        CubePos pos = CubePos.CODEC.decode(NbtOps.INSTANCE, tag.get("Pos"))
            .getOrThrow(false, s -> {}).getFirst();
        
        LevelChunkSection section = createEmptySection(level);
        
        // Deserialize section using Codecs (1.20.1 way)
        if (tag.contains("block_states")) {
            Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            
            Codec<PalettedContainer<BlockState>> blockStateCodec = PalettedContainer.codecRW(
                Block.BLOCK_STATE_REGISTRY,
                BlockState.CODEC,
                PalettedContainer.Strategy.SECTION_STATES,
                Blocks.AIR.defaultBlockState()
            );
            
            Codec<PalettedContainer<Holder<Biome>>> biomeCodec = PalettedContainer.codecRW(
                biomeRegistry.asHolderIdMap(),
                biomeRegistry.holderByNameCodec(),
                PalettedContainer.Strategy.SECTION_BIOMES,
                biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
            );
            
            PalettedContainer<BlockState> states = blockStateCodec
                .parse(NbtOps.INSTANCE, tag.getCompound("block_states"))
                .getOrThrow(false, s -> {});
            
            PalettedContainer<Holder<Biome>> biomes = biomeCodec
                .parse(NbtOps.INSTANCE, tag.getCompound("biomes"))
                .getOrThrow(false, s -> {});
            
            section = new LevelChunkSection(states, biomes);
        }
        
        Cube cube = new Cube(level, pos, section);
        cube.status = CubeStatus.valueOf(tag.getString("Status"));
        cube.inhabitedTime = tag.getLong("InhabitedTime");
        cube.lightCorrect = tag.getBoolean("LightCorrect");
        
        if (tag.contains("BlockLight")) {
            cube.blockLight = tag.getByteArray("BlockLight");
        }
        if (tag.contains("SkyLight")) {
            cube.skyLight = tag.getByteArray("SkyLight");
        }
        
        // Deserialize block entities (1.20.1 way - no Optional)
        ListTag beList = tag.getList("BlockEntities", 10);
        for (int i = 0; i < beList.size(); i++) {
            CompoundTag beTag = beList.getCompound(i);
            BlockPos bePos = BlockEntity.getPosFromTag(beTag);
            BlockState beState = cube.getBlockState(bePos);
            BlockEntity blockEntity = BlockEntity.loadStatic(bePos, beState, beTag);
            if (blockEntity != null) {
                cube.setBlockEntity(blockEntity);
            }
        }
        
        return cube;
    }
}