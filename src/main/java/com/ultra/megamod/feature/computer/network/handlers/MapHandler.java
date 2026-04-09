package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MapHandler {

    private static final String FILE_NAME = "megamod_waypoints.dat";
    private static final int MAX_WAYPOINTS_PER_PLAYER = 100;

    private static final Map<UUID, List<WaypointData>> playerWaypoints = new HashMap<>();
    private static boolean dirty = false;
    private static boolean loaded = false;

    public record WaypointData(String id, String name, int x, int y, int z,
        int colorIndex, long created, String category, String dimension, boolean beaconEnabled) {}

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
        }
    }

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        // Ensure data is loaded
        ensureLoaded(level);

        switch (action) {
            case "map_request_waypoints": {
                String json = buildWaypointsJson(player.getUUID());
                sendResponse(player, "map_waypoint_data", json, eco);
                return true;
            }

            case "map_save_waypoint": {
                // Parse waypoint from JSON
                String id = extractJsonString(jsonData, "id");
                String name = extractJsonString(jsonData, "name");
                int x = extractJsonInt(jsonData, "x");
                int y = extractJsonInt(jsonData, "y");
                int z = extractJsonInt(jsonData, "z");
                int colorIndex = extractJsonInt(jsonData, "colorIndex");
                String category = extractJsonString(jsonData, "category", "Other");
                String dimension = extractJsonString(jsonData, "dimension", "minecraft:overworld");
                boolean beaconEnabled = jsonData.contains("\"beaconEnabled\":true");

                if (name == null || name.trim().isEmpty()) {
                    name = "Waypoint";
                }
                // Sanitize name (max 24 chars)
                if (name.length() > 24) {
                    name = name.substring(0, 24);
                }
                // Clamp color
                colorIndex = Math.max(0, Math.min(7, colorIndex));

                List<WaypointData> wps = playerWaypoints.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());

                if (id != null && !id.isEmpty()) {
                    // Update existing waypoint
                    boolean found = false;
                    for (int i = 0; i < wps.size(); i++) {
                        if (wps.get(i).id.equals(id)) {
                            wps.set(i, new WaypointData(id, name, x, y, z, colorIndex,
                                wps.get(i).created, category, dimension, beaconEnabled));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // ID not found, treat as new
                        if (wps.size() >= MAX_WAYPOINTS_PER_PLAYER) {
                            sendResponse(player, "map_waypoint_saved", buildWaypointsJson(player.getUUID()), eco);
                            return true;
                        }
                        String newId = UUID.randomUUID().toString().substring(0, 8);
                        wps.add(new WaypointData(newId, name, x, y, z, colorIndex,
                            System.currentTimeMillis(), category, dimension, beaconEnabled));
                    }
                } else {
                    // New waypoint
                    if (wps.size() >= MAX_WAYPOINTS_PER_PLAYER) {
                        sendResponse(player, "map_waypoint_saved", buildWaypointsJson(player.getUUID()), eco);
                        return true;
                    }
                    String newId = UUID.randomUUID().toString().substring(0, 8);
                    wps.add(new WaypointData(newId, name, x, y, z, colorIndex,
                        System.currentTimeMillis(), category, dimension, beaconEnabled));
                }

                dirty = true;
                saveToDisk(level);

                String json = buildWaypointsJson(player.getUUID());
                sendResponse(player, "map_waypoint_saved", json, eco);
                return true;
            }

            case "map_delete_waypoint": {
                String wpId = jsonData.trim();
                List<WaypointData> wps = playerWaypoints.get(player.getUUID());
                if (wps != null) {
                    wps.removeIf(wp -> wp.id.equals(wpId));
                    dirty = true;
                    saveToDisk(level);
                }

                String json = buildWaypointsJson(player.getUUID());
                sendResponse(player, "map_waypoint_deleted", json, eco);
                return true;
            }

            case "map_share_waypoint": {
                String wpId = jsonData.trim();
                List<WaypointData> wps = playerWaypoints.get(player.getUUID());
                if (wps != null) {
                    for (WaypointData wp : wps) {
                        if (wp.id.equals(wpId)) {
                            String playerName = player.getGameProfile().name();
                            String msg = playerName + " shared waypoint [" + wp.name + "] at "
                                    + wp.x + ", " + wp.y + ", " + wp.z;
                            // Broadcast to all players on the server
                            for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
                                sp.sendSystemMessage(
                                    (Component) Component.literal((String) msg).withStyle(ChatFormatting.GOLD)
                                );
                            }
                            sendResponse(player, "map_waypoint_shared", "{\"success\":true}", eco);
                            return true;
                        }
                    }
                }
                sendResponse(player, "map_waypoint_shared", "{\"success\":false}", eco);
                return true;
            }

            case "map_bulk_waypoints": {
                String op = extractJsonString(jsonData, "operation", "");
                List<WaypointData> wps = playerWaypoints.get(player.getUUID());
                if (wps != null && !op.isEmpty()) {
                    switch (op) {
                        case "enable_all_beacons": {
                            List<WaypointData> updated = new ArrayList<>();
                            for (WaypointData wp : wps) {
                                updated.add(new WaypointData(wp.id(), wp.name(), wp.x(), wp.y(), wp.z(),
                                    wp.colorIndex(), wp.created(), wp.category(), wp.dimension(), true));
                            }
                            wps.clear();
                            wps.addAll(updated);
                            dirty = true;
                            saveToDisk(level);
                            break;
                        }
                        case "disable_all_beacons": {
                            List<WaypointData> updated = new ArrayList<>();
                            for (WaypointData wp : wps) {
                                updated.add(new WaypointData(wp.id(), wp.name(), wp.x(), wp.y(), wp.z(),
                                    wp.colorIndex(), wp.created(), wp.category(), wp.dimension(), false));
                            }
                            wps.clear();
                            wps.addAll(updated);
                            dirty = true;
                            saveToDisk(level);
                            break;
                        }
                        case "delete_category": {
                            String cat = extractJsonString(jsonData, "category", "");
                            if (!cat.isEmpty()) {
                                wps.removeIf(wp -> cat.equals(wp.category()));
                                dirty = true;
                                saveToDisk(level);
                            }
                            break;
                        }
                    }
                }
                String json = buildWaypointsJson(player.getUUID());
                sendResponse(player, "map_bulk_result", json, eco);
                return true;
            }

            case "map_share_waypoint_mail": {
                String waypointId = extractJsonString(jsonData, "waypointId", "");
                String targetName = extractJsonString(jsonData, "target", "");

                if (waypointId.isEmpty() || targetName.isEmpty()) {
                    sendResponse(player, "map_share_mail_result", "{\"success\":false,\"message\":\"Missing waypoint or target.\"}", eco);
                    return true;
                }

                List<WaypointData> wps = playerWaypoints.get(player.getUUID());
                WaypointData found = null;
                if (wps != null) {
                    for (WaypointData wp : wps) {
                        if (wp.id().equals(waypointId)) {
                            found = wp;
                            break;
                        }
                    }
                }

                if (found == null) {
                    sendResponse(player, "map_share_mail_result", "{\"success\":false,\"message\":\"Waypoint not found.\"}", eco);
                    return true;
                }

                // Resolve target UUID - check online players first
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target == null) {
                    sendResponse(player, "map_share_mail_result", "{\"success\":false,\"message\":\"Player not found or offline.\"}", eco);
                    return true;
                }

                UUID targetUuid = target.getUUID();
                String senderName = player.getGameProfile().name();
                String subject = "Shared Waypoint: " + found.name();
                String body = String.format(
                    "%s shared a waypoint with you!\n\nName: %s\nCoords: %d, %d, %d\nDimension: %s\nCategory: %s",
                    senderName, found.name(), found.x(), found.y(), found.z(), found.dimension(), found.category()
                );
                MailHandler.sendSystemMail(targetUuid, subject, body, level);

                sendResponse(player, "map_share_mail_result", "{\"success\":true,\"message\":\"Waypoint shared via mail to " + escapeJson(targetName) + ".\"}", eco);
                return true;
            }

            case "map_teleport": {
                // Admin only
                if (!AdminSystem.isAdmin(player)) {
                    sendResponse(player, "map_result", "{\"success\":false,\"message\":\"Admin only.\"}", eco);
                    return true;
                }
                try {
                    String[] parts = jsonData.split(":");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    player.teleportTo(level, x + 0.5, y, z + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
                } catch (Exception ignored) {}
                return true;
            }

            case "map_request_drawings": {
                MapDrawingManager.ensureLoaded(level);
                String dimension = extractJsonString(jsonData, "dimension", "minecraft:overworld");
                String json = MapDrawingManager.buildDrawingsJson(player.getUUID(), dimension);
                sendResponse(player, "map_drawing_data", json, eco);
                return true;
            }

            case "map_save_drawing_line": {
                MapDrawingManager.ensureLoaded(level);
                int x1 = extractJsonInt(jsonData, "x1");
                int z1 = extractJsonInt(jsonData, "z1");
                int x2 = extractJsonInt(jsonData, "x2");
                int z2 = extractJsonInt(jsonData, "z2");
                int color = extractJsonInt(jsonData, "color");
                String dimension = extractJsonString(jsonData, "dimension", "minecraft:overworld");
                String id = "line_" + UUID.randomUUID().toString().substring(0, 8);

                boolean shared = jsonData.contains("\"shared\":true");
                if (shared && AdminSystem.isAdmin(player)) {
                    MapDrawingManager.addSharedLine(
                        new MapDrawingManager.DrawingLine(id, x1, z1, x2, z2, color, dimension), level);
                } else {
                    MapDrawingManager.addLine(player.getUUID(),
                        new MapDrawingManager.DrawingLine(id, x1, z1, x2, z2, color, dimension), level);
                }

                String json = MapDrawingManager.buildDrawingsJson(player.getUUID(), dimension);
                sendResponse(player, "map_drawing_saved", json, eco);
                return true;
            }

            case "map_save_drawing_text": {
                MapDrawingManager.ensureLoaded(level);
                String text = extractJsonString(jsonData, "text", "");
                int x = extractJsonInt(jsonData, "x");
                int z = extractJsonInt(jsonData, "z");
                int color = extractJsonInt(jsonData, "color");
                String dimension = extractJsonString(jsonData, "dimension", "minecraft:overworld");
                String id = "text_" + UUID.randomUUID().toString().substring(0, 8);

                if (text.length() > 32) text = text.substring(0, 32);
                if (text.isEmpty()) text = "Label";

                boolean shared = jsonData.contains("\"shared\":true");
                if (shared && AdminSystem.isAdmin(player)) {
                    MapDrawingManager.addSharedText(
                        new MapDrawingManager.DrawingText(id, text, x, z, color, dimension), level);
                } else {
                    MapDrawingManager.addText(player.getUUID(),
                        new MapDrawingManager.DrawingText(id, text, x, z, color, dimension), level);
                }

                String json = MapDrawingManager.buildDrawingsJson(player.getUUID(), dimension);
                sendResponse(player, "map_drawing_saved", json, eco);
                return true;
            }

            case "map_delete_drawing": {
                MapDrawingManager.ensureLoaded(level);
                String drawingId = extractJsonString(jsonData, "id", "");
                String drawType = extractJsonString(jsonData, "type", "line");
                String dimension = extractJsonString(jsonData, "dimension", "minecraft:overworld");

                if ("line".equals(drawType)) {
                    MapDrawingManager.deleteLine(player.getUUID(), drawingId, level);
                } else {
                    MapDrawingManager.deleteText(player.getUUID(), drawingId, level);
                }

                String json = MapDrawingManager.buildDrawingsJson(player.getUUID(), dimension);
                sendResponse(player, "map_drawing_deleted", json, eco);
                return true;
            }

            case "map_request_highlights": {
                int centerX = extractJsonInt(jsonData, "centerX", player.getBlockX() >> 4);
                int centerZ = extractJsonInt(jsonData, "centerZ", player.getBlockZ() >> 4);
                int radius = extractJsonInt(jsonData, "radius", 16);
                var highlights = MapChunkScanner.scanArea(level, centerX, centerZ, radius);
                // Get world spawn from overworld's level data respawn data
                var respawnData = level.getServer().overworld().getLevelData().getRespawnData();
                int spawnX = respawnData.pos().getX();
                int spawnZ = respawnData.pos().getZ();
                sendResponse(player, "map_highlight_data", MapChunkScanner.buildHighlightsJson(highlights, spawnX, spawnZ), eco);
                return true;
            }

            case "map_request_structures": {
                String dim = level.dimension().identifier().toString();
                int cx = player.getBlockX() >> 4;
                int cz = player.getBlockZ() >> 4;
                MapStructureTracker.scanLoadedChunks(level, cx, cz, 16);
                sendResponse(player, "map_structure_data", MapStructureTracker.buildStructuresJson(dim), eco);
                return true;
            }

            case "map_locate_all_structures": {
                // Admin-only: use /locate to find ALL nearby structures
                if (!com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player)) {
                    sendResponse(player, "map_structure_data", "{\"structures\":[]}", eco);
                    return true;
                }
                String dim = level.dimension().identifier().toString();
                // Run /locate for each known structure type and add results to tracker
                String[][] structures = {
                    {"Village", "minecraft:village"}, {"Desert Temple", "minecraft:desert_pyramid"},
                    {"Jungle Temple", "minecraft:jungle_pyramid"}, {"Stronghold", "minecraft:stronghold"},
                    {"Monument", "minecraft:monument"}, {"Fortress", "minecraft:fortress"},
                    {"End City", "minecraft:end_city"}, {"Mansion", "minecraft:mansion"},
                    {"Ancient City", "minecraft:ancient_city"}, {"Pillager Outpost", "minecraft:pillager_outpost"},
                    {"Shipwreck", "minecraft:shipwreck"}, {"Ocean Ruin", "minecraft:ocean_ruin"},
                    {"Bastion", "minecraft:bastion_remnant"}, {"Ruined Portal", "minecraft:ruined_portal"},
                    {"Trail Ruins", "minecraft:trail_ruins"}, {"Trial Chambers", "minecraft:trial_chambers"},
                    {"Mineshaft", "minecraft:mineshaft"}, {"Igloo", "minecraft:igloo"},
                    {"Swamp Hut", "minecraft:swamp_hut"}
                };
                for (String[] s : structures) {
                    try {
                        String result = com.ultra.megamod.feature.computer.admin.AdminSystem.executeCommand(player, "locate structure " + s[1]);
                        // Parse "[X, ~, Z]" pattern
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[(-?\\d+),\\s*~?,?\\s*(-?\\d+)?(?:,\\s*)?(-?\\d+)]").matcher(result);
                        if (m.find()) {
                            int sx = Integer.parseInt(m.group(1));
                            int sz = Integer.parseInt(m.group(m.groupCount())); // last group is Z
                            MapStructureTracker.addStructure(dim, s[1], s[0], sx, sz);
                        }
                    } catch (Exception ignored) {}
                }
                sendResponse(player, "map_structure_data", MapStructureTracker.buildStructuresJson(dim), eco);
                return true;
            }

            default:
                return false;
        }
    }

    public static void addDeathMarker(UUID playerUuid, int x, int y, int z, String dimension, ServerLevel level) {
        ensureLoaded(level);
        List<WaypointData> waypoints = playerWaypoints.computeIfAbsent(playerUuid, k -> new ArrayList<>());

        // Count existing death markers and remove oldest if >= 5
        List<WaypointData> deaths = waypoints.stream()
            .filter(w -> "Death".equals(w.category())).collect(Collectors.toList());
        if (deaths.size() >= 5) {
            // Remove the oldest death marker (last in the sorted-by-created list)
            WaypointData oldest = deaths.get(deaths.size() - 1);
            waypoints.remove(oldest);
        }

        int deathNum = deaths.size() + 1;
        String id = "death_" + UUID.randomUUID().toString().substring(0, 8);
        WaypointData death = new WaypointData(id, "Death #" + deathNum, x, y, z,
            0, System.currentTimeMillis(), "Death", dimension, false);
        waypoints.add(0, death);
        dirty = true;
        saveToDisk(level);
    }

    private static String buildWaypointsJson(UUID playerId) {
        List<WaypointData> wps = playerWaypoints.getOrDefault(playerId, new ArrayList<>());
        StringBuilder sb = new StringBuilder("{\"waypoints\":[");
        for (int i = 0; i < wps.size(); i++) {
            if (i > 0) sb.append(",");
            WaypointData wp = wps.get(i);
            sb.append("{\"id\":\"").append(escapeJson(wp.id)).append("\"");
            sb.append(",\"name\":\"").append(escapeJson(wp.name)).append("\"");
            sb.append(",\"x\":").append(wp.x);
            sb.append(",\"y\":").append(wp.y);
            sb.append(",\"z\":").append(wp.z);
            sb.append(",\"colorIndex\":").append(wp.colorIndex);
            sb.append(",\"created\":").append(wp.created);
            sb.append(",\"category\":\"").append(escapeJson(wp.category)).append("\"");
            sb.append(",\"dimension\":\"").append(escapeJson(wp.dimension)).append("\"");
            sb.append(",\"beaconEnabled\":").append(wp.beaconEnabled);
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static void loadFromDisk(ServerLevel level) {
        playerWaypoints.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String uuidStr : players.keySet()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        CompoundTag playerTag = players.getCompoundOrEmpty(uuidStr);
                        ListTag wpList = playerTag.getListOrEmpty("waypoints");
                        List<WaypointData> wps = new ArrayList<>();
                        for (int i = 0; i < wpList.size(); i++) {
                            CompoundTag wpTag = wpList.getCompoundOrEmpty(i);
                            String id = wpTag.getStringOr("id", "");
                            String name = wpTag.getStringOr("name", "Waypoint");
                            int x = wpTag.getIntOr("x", 0);
                            int y = wpTag.getIntOr("y", 0);
                            int z = wpTag.getIntOr("z", 0);
                            int colorIndex = wpTag.getIntOr("colorIndex", 0);
                            long created = wpTag.getLongOr("created", 0L);
                            String category = wpTag.getStringOr("category", "Other");
                            String dimension = wpTag.getStringOr("dimension", "minecraft:overworld");
                            boolean beaconEnabled = wpTag.getBooleanOr("beaconEnabled", true);
                            if (!id.isEmpty()) {
                                wps.add(new WaypointData(id, name, x, y, z, colorIndex, created,
                                    category, dimension, beaconEnabled));
                            }
                        }
                        playerWaypoints.put(uuid, wps);
                    } catch (Exception ignored) {
                        // Skip malformed entries
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load map marker data", e);
        }
        loaded = true;
        dirty = false;
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();

            for (Map.Entry<UUID, List<WaypointData>> entry : playerWaypoints.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                ListTag wpList = new ListTag();
                for (WaypointData wp : entry.getValue()) {
                    CompoundTag wpTag = new CompoundTag();
                    wpTag.putString("id", wp.id);
                    wpTag.putString("name", wp.name);
                    wpTag.putInt("x", wp.x);
                    wpTag.putInt("y", wp.y);
                    wpTag.putInt("z", wp.z);
                    wpTag.putInt("colorIndex", wp.colorIndex);
                    wpTag.putLong("created", wp.created);
                    wpTag.putString("category", wp.category);
                    wpTag.putString("dimension", wp.dimension);
                    wpTag.putBoolean("beaconEnabled", wp.beaconEnabled);
                    wpList.add((Tag) wpTag);
                }
                playerTag.put("waypoints", (Tag) wpList);
                players.put(entry.getKey().toString(), (Tag) playerTag);
            }

            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save map marker data", e);
        }
    }

    public static void reset() {
        playerWaypoints.clear();
        dirty = false;
        loaded = false;
    }

    // ===================== JSON Helpers =====================

    private static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int valStart = idx + search.length();
        int valEnd = json.indexOf('"', valStart);
        if (valEnd < 0) return null;
        return json.substring(valStart, valEnd).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String extractJsonString(String json, String key, String defaultValue) {
        String result = extractJsonString(json, key);
        return result != null ? result : defaultValue;
    }

    private static int extractJsonInt(String json, String key) {
        if (json == null) return 0;
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        int valStart = idx + search.length();
        int valEnd = valStart;
        while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-')) {
            valEnd++;
        }
        try {
            return Integer.parseInt(json.substring(valStart, valEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int extractJsonInt(String json, String key, int defaultValue) {
        if (json == null) return defaultValue;
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultValue;
        int valStart = idx + search.length();
        int valEnd = valStart;
        while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-')) {
            valEnd++;
        }
        try {
            return Integer.parseInt(json.substring(valStart, valEnd));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(
            (ServerPlayer) player,
            (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
