package com.ultra.megamod.feature.worldedit.clipboard;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.schematic.data.SchematicData;
import com.ultra.megamod.feature.schematic.data.SchematicLoader;
import com.ultra.megamod.feature.schematic.data.SchematicMetadata;
import com.ultra.megamod.feature.schematic.data.SchematicRegion;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that bridges the WorldEdit clipboard with MegaMod's existing
 * SchematicData/.litematic format. Copy reads from the level into a
 * Clipboard; paste replays clipboard blocks into the level via an
 * EditSession.
 */
public final class ClipboardIO {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClipboardIO() {}

    /** Build a clipboard by reading every block in a selection. */
    public static Clipboard copyFromWorld(ServerLevel level, Region region, BlockPos anchor, String name) {
        BlockPos min = region.getMinimumPoint();
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
        for (BlockPos p : region) {
            BlockState s = level.getBlockState(p);
            if (s.isAir()) continue;
            BlockPos rel = p.subtract(min);
            blocks.put(rel.immutable(), s);
            var be = level.getBlockEntity(p);
            if (be != null) {
                blockEntities.put(rel.immutable(), be.saveWithFullMetadata(level.registryAccess()));
            }
        }
        Vec3i size = region.getDimensions();
        BlockPos offset = anchor == null ? BlockPos.ZERO : anchor.subtract(min);
        return new Clipboard(size, blocks, blockEntities, offset, name);
    }

    /** Paste clipboard blocks into the world. The at-origin parameter is
     *  where the player stood — the paste anchors relative to the
     *  clipboard's stored origin offset. */
    public static int pasteToWorld(EditSession es, Clipboard cb, BlockPos at, boolean skipAir) {
        BlockPos anchor = at.subtract(cb.getOrigin());
        int count = 0;
        for (Map.Entry<BlockPos, BlockState> e : cb.getBlocks().entrySet()) {
            if (skipAir && e.getValue().isAir()) continue;
            BlockPos world = anchor.offset(e.getKey());
            if (es.setBlock(world, e.getValue())) count++;
        }
        return count;
    }

    /** Converts a clipboard into a SchematicData, ready to be written as
     *  a .litematic via {@link #saveSchematic}. */
    public static SchematicData toSchematicData(Clipboard cb) {
        Map<BlockPos, BlockState> blocks = new HashMap<>(cb.getBlocks());
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>(cb.getBlockEntities());
        SchematicRegion region = new SchematicRegion(BlockPos.ZERO, cb.getSize(), blocks, blockEntities);
        SchematicMetadata meta = new SchematicMetadata(cb.getName(), "WorldEdit", "WorldEdit clipboard",
            cb.getSize(), System.currentTimeMillis(), System.currentTimeMillis(), blocks.size(), 1);
        return SchematicData.fromRegions(cb.getName(), meta, List.of(region));
    }

    /** Loads a .litematic into a clipboard. */
    public static Clipboard loadSchematicAsClipboard(Path path) {
        SchematicData data = SchematicLoader.load(path);
        if (data == null) return null;
        Map<BlockPos, BlockState> blocks = new HashMap<>(data.getBlocks());
        Map<BlockPos, CompoundTag> bes = new HashMap<>(data.getBlockEntities());
        return new Clipboard(data.getSize(), blocks, bes, BlockPos.ZERO, data.getName());
    }

    /** Writes a SchematicData as a compressed .litematic NBT file. */
    public static boolean saveSchematic(Path path, SchematicData data) {
        try {
            Files.createDirectories(path.getParent());
            CompoundTag root = encodeSchematic(data);
            NbtIo.writeCompressed(root, path);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save schematic to {}: {}", path, e.getMessage(), e);
            return false;
        }
    }

    private static CompoundTag encodeSchematic(SchematicData data) {
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 6);
        root.putInt("MinecraftDataVersion", 4082);

        CompoundTag meta = new CompoundTag();
        meta.putString("Name", data.getName());
        meta.putString("Author", data.getMetadata().getAuthor());
        meta.putString("Description", data.getMetadata().getDescription());
        Vec3i size = data.getSize();
        CompoundTag enc = new CompoundTag();
        enc.putInt("x", size.getX());
        enc.putInt("y", size.getY());
        enc.putInt("z", size.getZ());
        meta.put("EnclosingSize", enc);
        meta.putLong("TimeCreated", System.currentTimeMillis());
        meta.putLong("TimeModified", System.currentTimeMillis());
        meta.putInt("TotalBlocks", data.getBlocks().size());
        meta.putInt("RegionCount", 1);
        root.put("Metadata", meta);

        CompoundTag regions = new CompoundTag();
        CompoundTag region = new CompoundTag();
        CompoundTag pos = new CompoundTag();
        pos.putInt("x", 0); pos.putInt("y", 0); pos.putInt("z", 0);
        region.put("Position", pos);
        CompoundTag regionSize = new CompoundTag();
        regionSize.putInt("x", size.getX());
        regionSize.putInt("y", size.getY());
        regionSize.putInt("z", size.getZ());
        region.put("Size", regionSize);

        // Build palette
        LinkedHashMap<BlockState, Integer> palette = new LinkedHashMap<>();
        palette.put(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 0);
        for (BlockState s : data.getBlocks().values()) {
            palette.computeIfAbsent(s, k -> palette.size());
        }

        ListTag paletteTag = new ListTag();
        List<BlockState> ordered = new ArrayList<>(palette.keySet());
        ordered.sort(Comparator.comparingInt(palette::get));
        for (BlockState s : ordered) {
            paletteTag.add(encodePaletteEntry(s));
        }
        region.put("BlockStatePalette", paletteTag);

        int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        long volume = (long) size.getX() * size.getY() * size.getZ();
        long bitsTotal = volume * bits;
        int longs = (int) ((bitsTotal + 63L) / 64L);
        long[] arr = new long[longs];

        int sx = size.getX(), sy = size.getY(), sz = size.getZ();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    BlockPos k = new BlockPos(x, y, z);
                    BlockState s = data.getBlocks().get(k);
                    int id = s == null ? 0 : palette.get(s);
                    long idx = (long) y * sx * sz + (long) z * sx + x;
                    writePacked(arr, idx, bits, id);
                }
            }
        }
        region.put("BlockStates", new LongArrayTag(arr));
        region.put("TileEntities", new ListTag());
        region.put("Entities", new ListTag());
        region.put("PendingBlockTicks", new ListTag());
        region.put("PendingFluidTicks", new ListTag());

        regions.put(data.getName(), region);
        root.put("Regions", regions);
        return root;
    }

    private static CompoundTag encodePaletteEntry(BlockState s) {
        CompoundTag entry = new CompoundTag();
        var id = BuiltInRegistries.BLOCK.getKey(s.getBlock());
        entry.putString("Name", id.toString());
        if (!s.getValues().isEmpty()) {
            CompoundTag props = new CompoundTag();
            for (var e : s.getValues().entrySet()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Property prop = (Property) e.getKey();
                Comparable v = (Comparable) e.getValue();
                props.put(prop.getName(), StringTag.valueOf(prop.getName(v)));
            }
            entry.put("Properties", props);
        }
        return entry;
    }

    @SuppressWarnings("unused")
    private static void writePacked(long[] arr, long index, int bits, int value) {
        long maxEntry = (1L << bits) - 1L;
        long startOffset = index * bits;
        int startIdx = (int) (startOffset >> 6);
        int endIdx = (int) (((index + 1L) * bits - 1L) >> 6);
        int startBit = (int) (startOffset & 0x3F);
        long val = value & maxEntry;

        arr[startIdx] |= (val << startBit);
        if (startIdx != endIdx && endIdx < arr.length) {
            int endBit = 64 - startBit;
            arr[endIdx] |= (val >>> endBit);
        }
    }
}
