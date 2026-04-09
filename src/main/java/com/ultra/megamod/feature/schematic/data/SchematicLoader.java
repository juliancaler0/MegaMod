package com.ultra.megamod.feature.schematic.data;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads .litematic schematic files into SchematicData.
 * Ports the core NBT parsing from Litematica's LitematicaSchematic, adapted for NeoForge 1.21.11.
 *
 * .litematic format structure:
 *   Root: { Version: int, MinecraftDataVersion: int, Metadata: {...}, Regions: { regionName: {...}, ... } }
 *   Each region: { Position: {x,y,z}, Size: {x,y,z}, BlockStatePalette: [...], BlockStates: long[],
 *                   TileEntities: [...], Entities: [...] }
 *   BlockStates is a tight-packed long array where each entry is an index into the palette.
 */
public class SchematicLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SUPPORTED_VERSION = 7;
    private static final int MINIMUM_ENTRY_WIDTH = 2;

    /**
     * Loads a .litematic file from disk and returns the parsed SchematicData.
     */
    public static SchematicData load(Path path) {
        try {
            CompoundTag root = NbtIo.readCompressed(path, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            SchematicData result = readFromNbt(root, path.getFileName().toString());
            if (result != null) {
                result.setSourceFilePath(path.toAbsolutePath().toString());
            }
            return result;
        } catch (IOException e) {
            LOGGER.error("Failed to read schematic file: {}", path, e);
            return null;
        }
    }

    /**
     * Loads a .litematic from raw compressed NBT bytes.
     */
    public static SchematicData loadFromBytes(byte[] data, String fileName) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
            CompoundTag root = NbtIo.readCompressed(bais, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            return readFromNbt(root, fileName);
        } catch (IOException e) {
            LOGGER.error("Failed to read schematic from bytes: {}", fileName, e);
            return null;
        }
    }

    private static SchematicData readFromNbt(CompoundTag root, String fileName) {
        int version = root.getIntOr("Version", -1);
        if (version < 1 || version > MAX_SUPPORTED_VERSION) {
            LOGGER.warn("Unsupported schematic version: {} (max: {})", version, MAX_SUPPORTED_VERSION);
            if (version < 1) return null;
        }

        SchematicMetadata metadata = readMetadata(root);
        if (metadata.getName().isEmpty()) {
            // Use filename without extension as name
            String name = fileName;
            if (name.endsWith(".litematic")) {
                name = name.substring(0, name.length() - 10);
            }
            metadata.setName(name);
        }

        CompoundTag regionsTag = root.getCompoundOrEmpty("Regions");
        if (regionsTag.keySet().isEmpty()) {
            LOGGER.error("Schematic has no regions: {}", fileName);
            return null;
        }

        List<SchematicRegion> regions = new ArrayList<>();
        for (String regionName : regionsTag.keySet()) {
            Optional<CompoundTag> regionTagOpt = regionsTag.getCompound(regionName);
            if (regionTagOpt.isEmpty()) continue;
            CompoundTag regionTag = regionTagOpt.get();

            SchematicRegion region = readRegion(regionTag, regionName, version);
            if (region != null) {
                regions.add(region);
            }
        }

        if (regions.isEmpty()) {
            LOGGER.error("No valid regions found in schematic: {}", fileName);
            return null;
        }

        return SchematicData.fromRegions(metadata.getName(), metadata, regions);
    }

    private static SchematicMetadata readMetadata(CompoundTag root) {
        CompoundTag meta = root.getCompoundOrEmpty("Metadata");
        String name = meta.getStringOr("Name", "");
        String author = meta.getStringOr("Author", "");
        String description = meta.getStringOr("Description", "");
        long timeCreated = meta.getLongOr("TimeCreated", 0L);
        long timeModified = meta.getLongOr("TimeModified", 0L);
        int totalBlocks = meta.getIntOr("TotalBlocks", 0);
        int regionCount = meta.getIntOr("RegionCount", 0);

        Vec3i enclosingSize = Vec3i.ZERO;
        Optional<CompoundTag> sizeTag = meta.getCompound("EnclosingSize");
        if (sizeTag.isPresent()) {
            CompoundTag s = sizeTag.get();
            enclosingSize = new Vec3i(
                    Math.abs(s.getIntOr("x", 0)),
                    Math.abs(s.getIntOr("y", 0)),
                    Math.abs(s.getIntOr("z", 0))
            );
        }

        return new SchematicMetadata(name, author, description, enclosingSize,
                timeCreated, timeModified, totalBlocks, regionCount);
    }

    private static SchematicRegion readRegion(CompoundTag regionTag, String regionName, int version) {
        // Read position
        CompoundTag posTag = regionTag.getCompoundOrEmpty("Position");
        BlockPos regionPos = new BlockPos(
                posTag.getIntOr("x", 0),
                posTag.getIntOr("y", 0),
                posTag.getIntOr("z", 0)
        );

        // Read size (can be negative, meaning direction)
        CompoundTag sizeTag = regionTag.getCompoundOrEmpty("Size");
        int sizeX = sizeTag.getIntOr("x", 0);
        int sizeY = sizeTag.getIntOr("y", 0);
        int sizeZ = sizeTag.getIntOr("z", 0);

        // Absolute size for array indexing
        int absSizeX = Math.abs(sizeX);
        int absSizeY = Math.abs(sizeY);
        int absSizeZ = Math.abs(sizeZ);

        if (absSizeX == 0 || absSizeY == 0 || absSizeZ == 0) {
            LOGGER.warn("Region '{}' has zero-size dimension: {}x{}x{}", regionName, sizeX, sizeY, sizeZ);
            return null;
        }

        // Read palette
        ListTag paletteList = regionTag.getListOrEmpty("BlockStatePalette");
        if (paletteList.isEmpty()) {
            LOGGER.warn("Region '{}' has empty palette", regionName);
            return null;
        }

        BlockState[] palette = readPalette(paletteList);

        // Read block data (packed long array)
        long[] blockStates = regionTag.getLongArray("BlockStates").orElse(new long[0]);
        if (blockStates.length == 0) {
            LOGGER.warn("Region '{}' has empty block data", regionName);
            return null;
        }

        // Decode blocks from packed array
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();

        int bitsPerEntry = getRequiredBitWidth(palette.length);
        long volume = (long) absSizeX * absSizeY * absSizeZ;

        for (long i = 0; i < volume; i++) {
            int paletteId = getFromPackedArray(blockStates, i, bitsPerEntry);
            if (paletteId < 0 || paletteId >= palette.length) {
                continue;
            }

            BlockState state = palette[paletteId];
            if (state.isAir()) continue;

            // Convert flat index to x,y,z within region
            int y = (int) (i / ((long) absSizeX * absSizeZ));
            int remainder = (int) (i % ((long) absSizeX * absSizeZ));
            int z = remainder / absSizeX;
            int x = remainder % absSizeX;

            blocks.put(new BlockPos(x, y, z), state);
        }

        // Read block entities (tile entities)
        ListTag beList = regionTag.getListOrEmpty("TileEntities");
        if (beList.isEmpty()) {
            // Try alternate key name used in some versions
            beList = regionTag.getListOrEmpty("BlockEntities");
        }
        for (int i = 0; i < beList.size(); i++) {
            if (beList.get(i) instanceof CompoundTag beTag) {
                int x, y, z;
                if (version >= 2) {
                    x = beTag.getIntOr("x", 0);
                    y = beTag.getIntOr("y", 0);
                    z = beTag.getIntOr("z", 0);
                } else {
                    x = beTag.getIntOr("x", 0);
                    y = beTag.getIntOr("y", 0);
                    z = beTag.getIntOr("z", 0);
                }
                blockEntities.put(new BlockPos(x, y, z), beTag);
            }
        }

        return new SchematicRegion(regionPos, new Vec3i(absSizeX, absSizeY, absSizeZ), blocks, blockEntities);
    }

    /**
     * Reads the BlockStatePalette from NBT and resolves each entry to a BlockState.
     * Each palette entry is a CompoundTag with "Name" (block ID) and optionally "Properties".
     */
    private static BlockState[] readPalette(ListTag paletteList) {
        BlockState[] palette = new BlockState[paletteList.size()];

        for (int i = 0; i < paletteList.size(); i++) {
            if (paletteList.get(i) instanceof CompoundTag entry) {
                palette[i] = resolveBlockState(entry);
            } else {
                palette[i] = Blocks.AIR.defaultBlockState();
            }
        }

        return palette;
    }

    /**
     * Resolves a palette entry CompoundTag to a BlockState.
     * Format: { "Name": "minecraft:stone", "Properties": { "variant": "granite" } }
     */
    private static BlockState resolveBlockState(CompoundTag entry) {
        String blockName = entry.getStringOr("Name", "minecraft:air");

        Identifier blockId = Identifier.tryParse(blockName);
        if (blockId == null) {
            LOGGER.warn("Invalid block identifier: {}", blockName);
            return Blocks.AIR.defaultBlockState();
        }

        Optional<Block> blockOpt = BuiltInRegistries.BLOCK.getOptional(blockId);
        if (blockOpt.isEmpty()) {
            LOGGER.warn("Unknown block: {}", blockName);
            return Blocks.AIR.defaultBlockState();
        }

        Block block = blockOpt.get();
        BlockState state = block.defaultBlockState();

        // Apply properties if present
        Optional<CompoundTag> propsOpt = entry.getCompound("Properties");
        if (propsOpt.isPresent()) {
            CompoundTag props = propsOpt.get();
            for (String propName : props.keySet()) {
                String propValue = props.getStringOr(propName, "");
                state = applyProperty(state, propName, propValue);
            }
        }

        return state;
    }

    /**
     * Applies a single property value to a BlockState.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(BlockState state, String propName, String propValue) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(propName);
        if (property == null) return state;

        Optional<?> valueOpt = property.getValue(propValue);
        if (valueOpt.isPresent()) {
            return state.setValue((Property) property, (Comparable) valueOpt.get());
        }
        return state;
    }

    /**
     * Gets the required bit width for a palette of the given size.
     * Matches Litematica's ArrayBlockContainer.getRequiredBitWidth().
     * Minimum is 2 bits per entry.
     */
    static int getRequiredBitWidth(int paletteSize) {
        return Math.max(MINIMUM_ENTRY_WIDTH, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1));
    }

    /**
     * Reads a value from a tight-packed long array.
     * Ported from Litematica's TightLongBackedIntArray.getAt().
     *
     * In the tight-packed format, entries can span across long boundaries.
     * This is different from the aligned format where entries are padded to not cross boundaries.
     */
    static int getFromPackedArray(long[] array, long index, int bitsPerEntry) {
        long maxEntryValue = (1L << bitsPerEntry) - 1L;
        long startOffset = index * bitsPerEntry;
        int startArrIndex = (int) (startOffset >> 6); // startOffset / 64
        int endArrIndex = (int) (((index + 1L) * bitsPerEntry - 1L) >> 6);
        int startBitOffset = (int) (startOffset & 0x3F); // startOffset % 64

        if (startArrIndex >= array.length) return 0;

        if (startArrIndex == endArrIndex) {
            return (int) ((array[startArrIndex] >>> startBitOffset) & maxEntryValue);
        } else {
            if (endArrIndex >= array.length) return 0;
            int endOffset = 64 - startBitOffset;
            return (int) (((array[startArrIndex] >>> startBitOffset) | (array[endArrIndex] << endOffset)) & maxEntryValue);
        }
    }

    /**
     * Converts a .litematic schematic file to compressed NBT bytes.
     * Used for network transfer.
     */
    public static byte[] fileToBytes(Path path) {
        try {
            return java.nio.file.Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Failed to read schematic file bytes: {}", path, e);
            return null;
        }
    }
}
