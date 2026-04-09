package com.ultra.megamod.feature.citizen.blueprint;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Static utility for reading and writing .blueprint files (NBT format) for the colony system.
 * <p>
 * NBT structure:
 * <pre>
 * {
 *   version: byte,
 *   size_x: short, size_y: short, size_z: short,
 *   palette: ListTag of block state strings "namespace:path[prop1=val1,prop2=val2]",
 *   blocks: int[] (packed pairs of shorts),
 *   tile_entities: ListTag of CompoundTags with x,y,z shorts,
 *   entities: ListTag of CompoundTags with Pos list,
 *   required_mods: ListTag of StringTags,
 *   name: string,
 *   architects: ListTag of StringTags,
 *   pack_name: string,
 *   primary_offset: {x: int, y: int, z: int}
 * }
 * </pre>
 */
public final class BlueprintUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Current blueprint format version. */
    private static final byte CURRENT_VERSION = 1;

    private BlueprintUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========== Read ==========

    /**
     * Reads a Blueprint from a compressed NBT file on disk.
     *
     * @param path the file path to read from
     * @return the deserialized Blueprint, or null on failure
     */
    @Nullable
    public static Blueprint readBlueprintFromFile(Path path) {
        try {
            CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            Blueprint bp = readBlueprintFromNBT(tag);
            if (bp != null) {
                bp.setFilePath(path.getParent());
                bp.setFileName(path.getFileName().toString());
            }
            return bp;
        } catch (IOException e) {
            LOGGER.error("Failed to read blueprint file: {}", path, e);
            return null;
        }
    }

    /**
     * Deserializes a Blueprint from the given CompoundTag.
     *
     * @param tag the root NBT tag
     * @return the deserialized Blueprint, or null if the format is invalid
     */
    @Nullable
    public static Blueprint readBlueprintFromNBT(CompoundTag tag) {
        byte version = tag.getByteOr("version", (byte) 0);
        if (version != CURRENT_VERSION) {
            LOGGER.warn("Unsupported blueprint version: {}", version);
            if (version < 1) return null;
        }

        short sizeX = tag.getShortOr("size_x", (short) 0);
        short sizeY = tag.getShortOr("size_y", (short) 0);
        short sizeZ = tag.getShortOr("size_z", (short) 0);

        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            LOGGER.error("Invalid blueprint dimensions: {}x{}x{}", sizeX, sizeY, sizeZ);
            return null;
        }

        // Read required mods
        List<String> requiredMods = new ArrayList<>();
        ListTag modsList = tag.getListOrEmpty("required_mods");
        for (int i = 0; i < modsList.size(); i++) {
            requiredMods.add(modsList.getString(i).orElse(""));
        }

        // Read palette
        ListTag paletteTag = tag.getListOrEmpty("palette");
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++) {
            if (paletteTag.get(i) instanceof CompoundTag entry) {
                palette.add(readBlockStateFromTag(entry));
            } else if (paletteTag.get(i) instanceof StringTag strTag) {
                palette.add(blockStateFromString(strTag.value()));
            } else {
                palette.add(Blocks.AIR.defaultBlockState());
            }
        }

        if (palette.isEmpty()) {
            LOGGER.error("Blueprint has empty palette");
            return null;
        }

        // Read blocks
        int[] blockInts = tag.getIntArray("blocks").orElse(new int[0]);
        short[][][] blocks = convertSaveDataToBlocks(blockInts, sizeX, sizeY, sizeZ);

        // Read tile entities
        ListTag tileEntitiesTag = tag.getListOrEmpty("tile_entities");
        CompoundTag[] tileEntities = new CompoundTag[tileEntitiesTag.size()];
        for (int i = 0; i < tileEntitiesTag.size(); i++) {
            tileEntities[i] = tileEntitiesTag.getCompound(i).orElse(new CompoundTag());
        }

        // Read entities
        ListTag entitiesTag = tag.getListOrEmpty("entities");
        CompoundTag[] entities = new CompoundTag[entitiesTag.size()];
        for (int i = 0; i < entitiesTag.size(); i++) {
            entities[i] = entitiesTag.getCompound(i).orElse(new CompoundTag());
        }

        Blueprint blueprint = new Blueprint(sizeX, sizeY, sizeZ, (short) palette.size(),
                palette, blocks, tileEntities, requiredMods);
        blueprint.setEntities(entities);

        // Read name
        String name = tag.getStringOr("name", "");
        if (!name.isEmpty()) {
            blueprint.setName(name);
        }

        // Read pack name
        String packName = tag.getStringOr("pack_name", "");
        if (!packName.isEmpty()) {
            blueprint.setPackName(packName);
        }

        // Read architects
        ListTag architectsTag = tag.getListOrEmpty("architects");
        if (!architectsTag.isEmpty()) {
            String[] architects = new String[architectsTag.size()];
            for (int i = 0; i < architectsTag.size(); i++) {
                architects[i] = architectsTag.getString(i).orElse("");
            }
            blueprint.setArchitects(architects);
        }

        // Read primary offset from optional data
        CompoundTag optionalData = tag.getCompoundOrEmpty("optional_data");
        CompoundTag megamodData = optionalData.getCompoundOrEmpty("megamod");
        if (megamodData.contains("primary_offset")) {
            CompoundTag offsetTag = megamodData.getCompoundOrEmpty("primary_offset");
            BlockPos offset = new BlockPos(
                    offsetTag.getIntOr("x", sizeX / 2),
                    offsetTag.getIntOr("y", 0),
                    offsetTag.getIntOr("z", sizeZ / 2));
            blueprint.setCachePrimaryOffset(offset);
        }

        return blueprint;
    }

    // ========== Write ==========

    /**
     * Writes a Blueprint to a compressed NBT file on disk.
     *
     * @param blueprint the blueprint to write
     * @param path      the target file path
     */
    public static void writeBlueprintToFile(Blueprint blueprint, Path path) {
        try {
            NbtIo.writeCompressed(writeBlueprintToNBT(blueprint), path);
        } catch (IOException e) {
            LOGGER.error("Failed to write blueprint file: {}", path, e);
        }
    }

    /**
     * Serializes a Blueprint to a CompoundTag.
     *
     * @param blueprint the blueprint to serialize
     * @return the serialized NBT
     */
    public static CompoundTag writeBlueprintToNBT(Blueprint blueprint) {
        CompoundTag tag = new CompoundTag();

        tag.putByte("version", CURRENT_VERSION);
        tag.putShort("size_x", blueprint.getSizeX());
        tag.putShort("size_y", blueprint.getSizeY());
        tag.putShort("size_z", blueprint.getSizeZ());

        // Write palette
        BlockState[] palette = blueprint.getPalette();
        ListTag paletteTag = new ListTag();
        for (int i = 0; i < blueprint.getPaletteSize(); i++) {
            paletteTag.add(StringTag.valueOf(blockStateToString(palette[i])));
        }
        tag.put("palette", paletteTag);

        // Write blocks
        int[] blockInts = convertBlocksToSaveData(
                blueprint.getStructure(), blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
        tag.putIntArray("blocks", blockInts);

        // Write tile entities
        ListTag tileEntitiesTag = new ListTag();
        CompoundTag[][][] tes = blueprint.getTileEntities();
        for (CompoundTag[][] plane : tes) {
            for (CompoundTag[] row : plane) {
                for (CompoundTag te : row) {
                    if (te != null) {
                        tileEntitiesTag.add(te);
                    }
                }
            }
        }
        tag.put("tile_entities", tileEntitiesTag);

        // Write entities
        ListTag entitiesTag = new ListTag();
        for (CompoundTag entity : blueprint.getEntities()) {
            if (entity != null) {
                entitiesTag.add(entity);
            }
        }
        tag.put("entities", entitiesTag);

        // Write required mods
        ListTag modsTag = new ListTag();
        for (String mod : blueprint.getRequiredMods()) {
            modsTag.add(StringTag.valueOf(mod));
        }
        tag.put("required_mods", modsTag);

        // Write metadata
        if (blueprint.getName() != null) {
            tag.putString("name", blueprint.getName());
        }
        if (blueprint.getPackName() != null) {
            tag.putString("pack_name", blueprint.getPackName());
        }
        if (blueprint.getArchitects() != null) {
            ListTag architectsTag = new ListTag();
            for (String architect : blueprint.getArchitects()) {
                architectsTag.add(StringTag.valueOf(architect));
            }
            tag.put("architects", architectsTag);
        }

        // Write primary offset in optional data
        CompoundTag optionalTag = new CompoundTag();
        CompoundTag megamodTag = new CompoundTag();
        BlockPos offset = blueprint.getPrimaryBlockOffset();
        CompoundTag offsetTag = new CompoundTag();
        offsetTag.putInt("x", offset.getX());
        offsetTag.putInt("y", offset.getY());
        offsetTag.putInt("z", offset.getZ());
        megamodTag.put("primary_offset", offsetTag);
        optionalTag.put("megamod", megamodTag);
        tag.put("optional_data", optionalTag);

        return tag;
    }

    // ========== Block state serialization ==========

    /**
     * Converts a BlockState to the string format "namespace:path[prop1=val1,prop2=val2]".
     */
    static String blockStateToString(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.getValues().isEmpty()) {
            return id.toString();
        }
        StringBuilder sb = new StringBuilder(id.toString());
        sb.append('[');
        boolean first = true;
        for (var entry : state.getValues().entrySet()) {
            if (!first) sb.append(',');
            sb.append(entry.getKey().getName()).append('=')
                    .append(propertyValueToString(entry.getKey(), entry.getValue()));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String propertyValueToString(Property property, Comparable value) {
        return property.getName(value);
    }

    /**
     * Parses a block state string like "minecraft:oak_stairs[facing=north,half=bottom]".
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static BlockState blockStateFromString(String str) {
        int bracketStart = str.indexOf('[');
        String blockName = bracketStart > 0 ? str.substring(0, bracketStart) : str;
        Identifier blockId = Identifier.tryParse(blockName);
        if (blockId == null) {
            LOGGER.warn("Invalid block identifier in palette: {}", blockName);
            return Blocks.AIR.defaultBlockState();
        }

        Optional<Block> blockOpt = BuiltInRegistries.BLOCK.getOptional(blockId);
        if (blockOpt.isEmpty()) {
            LOGGER.warn("Unknown block in palette: {}", blockName);
            return Blocks.AIR.defaultBlockState();
        }

        BlockState state = blockOpt.get().defaultBlockState();

        if (bracketStart > 0 && str.endsWith("]")) {
            String propsStr = str.substring(bracketStart + 1, str.length() - 1);
            for (String propStr : propsStr.split(",")) {
                String[] kv = propStr.split("=", 2);
                if (kv.length == 2) {
                    Property<?> prop = state.getBlock().getStateDefinition().getProperty(kv[0]);
                    if (prop != null) {
                        Optional<?> val = prop.getValue(kv[1]);
                        if (val.isPresent()) {
                            state = state.setValue((Property) prop, (Comparable) val.get());
                        }
                    }
                }
            }
        }
        return state;
    }

    /**
     * Reads a BlockState from a CompoundTag with "Name" and optional "Properties" keys.
     * This supports the Structurize/vanilla NBT palette format.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState readBlockStateFromTag(CompoundTag entry) {
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

        BlockState state = blockOpt.get().defaultBlockState();

        Optional<CompoundTag> propsOpt = entry.getCompound("Properties");
        if (propsOpt.isPresent()) {
            CompoundTag props = propsOpt.get();
            for (String propName : props.keySet()) {
                String propValue = props.getStringOr(propName, "");
                Property<?> prop = state.getBlock().getStateDefinition().getProperty(propName);
                if (prop != null) {
                    Optional<?> val = prop.getValue(propValue);
                    if (val.isPresent()) {
                        state = state.setValue((Property) prop, (Comparable) val.get());
                    }
                }
            }
        }

        return state;
    }

    // ========== Block data packing ==========

    /**
     * Converts a 3D short array [y][z][x] to a packed int array for NBT storage.
     * Each int packs two shorts: high 16 bits = first short, low 16 bits = second short.
     */
    static int[] convertBlocksToSaveData(short[][][] blocks, short sizeX, short sizeY, short sizeZ) {
        short[] flat = new short[sizeX * sizeY * sizeZ];
        int idx = 0;
        for (short y = 0; y < sizeY; y++) {
            for (short z = 0; z < sizeZ; z++) {
                for (short x = 0; x < sizeX; x++) {
                    flat[idx++] = blocks[y][z][x];
                }
            }
        }

        int[] ints = new int[(int) Math.ceil(flat.length / 2.0)];
        for (int i = 1; i < flat.length; i += 2) {
            ints[(i - 1) / 2] = (flat[i - 1] << 16) | (flat[i] & 0xFFFF);
        }
        if (flat.length % 2 == 1) {
            ints[ints.length - 1] = flat[flat.length - 1] << 16;
        }
        return ints;
    }

    /**
     * Converts a packed int array back to a 3D short array [y][z][x].
     */
    static short[][][] convertSaveDataToBlocks(int[] ints, short sizeX, short sizeY, short sizeZ) {
        short[] flat = new short[ints.length * 2];
        for (int i = 0; i < ints.length; i++) {
            flat[i * 2] = (short) (ints[i] >> 16);
            flat[i * 2 + 1] = (short) ints[i];
        }

        short[][][] blocks = new short[sizeY][sizeZ][sizeX];
        int idx = 0;
        for (short y = 0; y < sizeY; y++) {
            for (short z = 0; z < sizeZ; z++) {
                for (short x = 0; x < sizeX; x++) {
                    if (idx < flat.length) {
                        blocks[y][z][x] = flat[idx++];
                    }
                }
            }
        }
        return blocks;
    }
}
