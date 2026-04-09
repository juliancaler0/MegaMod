package com.ultra.megamod.feature.schematic.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core in-memory representation of a loaded schematic.
 * Blocks are stored in a map keyed by relative position (air omitted).
 * All regions from the source file are merged into a single block map.
 */
public class SchematicData {

    private final String name;
    private String sourceFilePath; // original file path for re-reading on accept
    private final Vec3i size;
    private final Map<BlockPos, BlockState> blocks;
    private final Map<BlockPos, CompoundTag> blockEntities;
    private final SchematicMetadata metadata;
    private final List<SchematicRegion> regions;

    public SchematicData(String name, Vec3i size,
                         Map<BlockPos, BlockState> blocks,
                         Map<BlockPos, CompoundTag> blockEntities,
                         SchematicMetadata metadata,
                         List<SchematicRegion> regions) {
        this.name = name;
        this.size = size;
        this.blocks = Collections.unmodifiableMap(blocks);
        this.blockEntities = Collections.unmodifiableMap(blockEntities);
        this.metadata = metadata;
        this.regions = List.copyOf(regions);
    }

    public String getName() { return name; }
    public String getSourceFilePath() { return sourceFilePath; }
    public void setSourceFilePath(String path) { this.sourceFilePath = path; }
    public Vec3i getSize() { return size; }
    public Map<BlockPos, BlockState> getBlocks() { return blocks; }
    public Map<BlockPos, CompoundTag> getBlockEntities() { return blockEntities; }
    public SchematicMetadata getMetadata() { return metadata; }
    public List<SchematicRegion> getRegions() { return regions; }

    public int getTotalBlockCount() {
        return blocks.size();
    }

    /**
     * Creates a SchematicData by merging multiple regions into a single block map.
     */
    public static SchematicData fromRegions(String name, SchematicMetadata metadata,
                                            List<SchematicRegion> regions) {
        Map<BlockPos, BlockState> mergedBlocks = new HashMap<>();
        Map<BlockPos, CompoundTag> mergedBlockEntities = new HashMap<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (SchematicRegion region : regions) {
            BlockPos offset = region.getRelativePosition();
            for (Map.Entry<BlockPos, BlockState> entry : region.getBlocks().entrySet()) {
                BlockPos worldPos = entry.getKey().offset(offset);
                mergedBlocks.put(worldPos, entry.getValue());

                minX = Math.min(minX, worldPos.getX());
                minY = Math.min(minY, worldPos.getY());
                minZ = Math.min(minZ, worldPos.getZ());
                maxX = Math.max(maxX, worldPos.getX());
                maxY = Math.max(maxY, worldPos.getY());
                maxZ = Math.max(maxZ, worldPos.getZ());
            }
            for (Map.Entry<BlockPos, CompoundTag> entry : region.getBlockEntities().entrySet()) {
                mergedBlockEntities.put(entry.getKey().offset(offset), entry.getValue());
            }
        }

        Vec3i size;
        if (metadata.getEnclosingSize() != null && !metadata.getEnclosingSize().equals(Vec3i.ZERO)) {
            size = metadata.getEnclosingSize();
        } else if (mergedBlocks.isEmpty()) {
            size = Vec3i.ZERO;
        } else {
            size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        }

        // Normalize positions so minimum corner is (0,0,0)
        if (!mergedBlocks.isEmpty() && (minX != 0 || minY != 0 || minZ != 0)) {
            Map<BlockPos, BlockState> normalized = new HashMap<>(mergedBlocks.size());
            Map<BlockPos, CompoundTag> normalizedBE = new HashMap<>(mergedBlockEntities.size());
            for (Map.Entry<BlockPos, BlockState> entry : mergedBlocks.entrySet()) {
                normalized.put(entry.getKey().offset(-minX, -minY, -minZ), entry.getValue());
            }
            for (Map.Entry<BlockPos, CompoundTag> entry : mergedBlockEntities.entrySet()) {
                normalizedBE.put(entry.getKey().offset(-minX, -minY, -minZ), entry.getValue());
            }
            mergedBlocks = normalized;
            mergedBlockEntities = normalizedBE;
        }

        metadata.setTotalBlocks(mergedBlocks.size());
        metadata.setRegionCount(regions.size());

        return new SchematicData(name, size, mergedBlocks, mergedBlockEntities, metadata, regions);
    }
}
