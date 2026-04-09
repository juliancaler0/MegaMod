package com.ultra.megamod.feature.computer.network.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;

public class MapStructureTracker {

    public record StructureMarker(String type, String displayName, int x, int z) {}

    // Cached structures per dimension
    private static final Map<String, List<StructureMarker>> dimensionStructures = new HashMap<>();
    // Track which chunks we've already scanned
    private static final Map<String, Set<Long>> scannedChunks = new HashMap<>();

    // Known structure types and display names
    private static final Map<String, String> STRUCTURE_NAMES = new HashMap<>();
    static {
        STRUCTURE_NAMES.put("minecraft:village_plains", "Village");
        STRUCTURE_NAMES.put("minecraft:village_desert", "Village");
        STRUCTURE_NAMES.put("minecraft:village_savanna", "Village");
        STRUCTURE_NAMES.put("minecraft:village_snowy", "Village");
        STRUCTURE_NAMES.put("minecraft:village_taiga", "Village");
        STRUCTURE_NAMES.put("minecraft:stronghold", "Stronghold");
        STRUCTURE_NAMES.put("minecraft:monument", "Monument");
        STRUCTURE_NAMES.put("minecraft:fortress", "Nether Fortress");
        STRUCTURE_NAMES.put("minecraft:desert_pyramid", "Desert Temple");
        STRUCTURE_NAMES.put("minecraft:jungle_pyramid", "Jungle Temple");
        STRUCTURE_NAMES.put("minecraft:igloo", "Igloo");
        STRUCTURE_NAMES.put("minecraft:shipwreck", "Shipwreck");
        STRUCTURE_NAMES.put("minecraft:ocean_ruin_cold", "Ocean Ruin");
        STRUCTURE_NAMES.put("minecraft:ocean_ruin_warm", "Ocean Ruin");
        STRUCTURE_NAMES.put("minecraft:buried_treasure", "Buried Treasure");
        STRUCTURE_NAMES.put("minecraft:pillager_outpost", "Pillager Outpost");
        STRUCTURE_NAMES.put("minecraft:mansion", "Woodland Mansion");
        STRUCTURE_NAMES.put("minecraft:bastion_remnant", "Bastion");
        STRUCTURE_NAMES.put("minecraft:ruined_portal", "Ruined Portal");
        STRUCTURE_NAMES.put("minecraft:ancient_city", "Ancient City");
        STRUCTURE_NAMES.put("minecraft:trail_ruins", "Trail Ruins");
        STRUCTURE_NAMES.put("minecraft:trial_chambers", "Trial Chambers");
        STRUCTURE_NAMES.put("minecraft:end_city", "End City");
        STRUCTURE_NAMES.put("minecraft:mineshaft", "Mineshaft");
        STRUCTURE_NAMES.put("minecraft:witch_hut", "Witch Hut");
    }

    /**
     * Scan loaded chunks for structure starts and add to cache.
     */
    public static void scanLoadedChunks(ServerLevel level, int centerChunkX, int centerChunkZ, int radius) {
        String dimKey = level.dimension().identifier().toString();
        List<StructureMarker> structures = dimensionStructures.computeIfAbsent(dimKey, k -> new ArrayList<>());
        Set<Long> scanned = scannedChunks.computeIfAbsent(dimKey, k -> new HashSet<>());

        radius = Math.min(radius, 32);

        for (int cx = centerChunkX - radius; cx <= centerChunkX + radius; cx++) {
            for (int cz = centerChunkZ - radius; cz <= centerChunkZ + radius; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                if (scanned.contains(key)) continue;
                if (!level.hasChunk(cx, cz)) continue;

                scanned.add(key);
                LevelChunk chunk = level.getChunk(cx, cz);

                // Get all structure starts in this chunk
                Map<Structure, StructureStart> starts = chunk.getAllStarts();
                for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
                    StructureStart start = entry.getValue();
                    if (start == null || !start.isValid()) continue;

                    BlockPos startPos = start.getChunkPos().getMiddleBlockPosition(64);
                    // Try to get the structure's registry name
                    String structureId = "unknown";
                    try {
                        var reg = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
                        var regId = reg.getKey(entry.getKey());
                        if (regId != null) {
                            structureId = regId.toString();
                        }
                    } catch (Exception ignored) {}

                    String displayName = STRUCTURE_NAMES.getOrDefault(structureId, prettifyName(structureId));

                    // Avoid duplicate entries for same structure
                    boolean duplicate = false;
                    for (StructureMarker s : structures) {
                        if (s.type().equals(displayName)
                                && Math.abs(s.x() - startPos.getX()) < 64
                                && Math.abs(s.z() - startPos.getZ()) < 64) {
                            duplicate = true;
                            break;
                        }
                    }

                    if (!duplicate) {
                        structures.add(new StructureMarker(structureId, displayName, startPos.getX(), startPos.getZ()));
                    }
                }
            }
        }
    }

    public static String buildStructuresJson(String dimension) {
        List<StructureMarker> structures = dimensionStructures.getOrDefault(dimension, List.of());
        StringBuilder sb = new StringBuilder("{\"structures\":[");
        boolean first = true;
        for (StructureMarker s : structures) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"type\":\"").append(escapeJson(s.type()))
              .append("\",\"name\":\"").append(escapeJson(s.displayName()))
              .append("\",\"x\":").append(s.x())
              .append(",\"z\":").append(s.z()).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String prettifyName(String id) {
        if (id.contains(":")) id = id.substring(id.indexOf(':') + 1);
        String spaced = id.replace('_', ' ');
        if (spaced.isEmpty()) return "Unknown";
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Add a structure from admin /locate command.
     * Deduplicates against existing entries.
     */
    public static void addStructure(String dimension, String type, String displayName, int x, int z) {
        List<StructureMarker> structures = dimensionStructures.computeIfAbsent(dimension, k -> new ArrayList<>());
        boolean duplicate = structures.stream().anyMatch(s ->
            s.displayName().equals(displayName) &&
            Math.abs(s.x() - x) < 64 && Math.abs(s.z() - z) < 64);
        if (!duplicate) {
            structures.add(new StructureMarker(type, displayName, x, z));
        }
    }

    public static void clearCache() {
        dimensionStructures.clear();
        scannedChunks.clear();
    }
}
