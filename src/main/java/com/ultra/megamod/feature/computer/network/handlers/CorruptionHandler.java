package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.corruption.CorruptionManager;
import com.ultra.megamod.feature.corruption.CorruptionManager.CorruptionZone;
import com.ultra.megamod.feature.corruption.PurgeManager;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class CorruptionHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "corruption_request": {
                sendCorruptionData(player, level, eco);
                return true;
            }
            case "corruption_create_zone": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleCreateZone(player, jsonData, level, eco);
                return true;
            }
            case "corruption_remove_zone": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleRemoveZone(player, jsonData, level, eco);
                return true;
            }
            case "corruption_start_purge": {
                handleStartPurge(player, jsonData, level, eco);
                return true;
            }
            case "corruption_purge_status": {
                sendPurgeStatus(player, level, eco);
                return true;
            }
            case "corruption_set_tier": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleSetTier(player, jsonData, level, eco);
                return true;
            }
            case "corruption_toggle": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleToggleSpread(player, level, eco);
                return true;
            }
            case "corruption_clear_all": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleClearAll(player, level, eco);
                return true;
            }
            // Keep backward compatibility with old action names
            case "corruption_create": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleCreateZoneLegacy(player, jsonData, level, eco);
                return true;
            }
            case "corruption_remove": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleRemoveZone(player, jsonData, level, eco);
                return true;
            }
            case "corruption_modify": {
                if (!AdminSystem.isAdmin(player)) return true;
                handleModify(player, jsonData, level, eco);
                return true;
            }
            case "corruption_purge_start": {
                handleStartPurge(player, jsonData, level, eco);
                return true;
            }
            case "corruption_purge_stop": {
                if (!AdminSystem.isAdmin(player)) return true;
                handlePurgeStop(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void sendCorruptionData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        CorruptionManager cm = CorruptionManager.get(overworld);
        PurgeManager pm = PurgeManager.get(overworld);
        boolean isAdmin = AdminSystem.isAdmin(player);

        JsonObject root = new JsonObject();

        // Zones array
        JsonArray zonesArr = new JsonArray();
        List<CorruptionZone> zonesToShow;
        if (isAdmin) {
            zonesToShow = cm.getAllZones();
        } else {
            // Non-admin: only nearby zones (within 512 blocks)
            zonesToShow = cm.getZonesInRange(player.blockPosition(), 512);
        }

        for (CorruptionZone zone : zonesToShow) {
            JsonObject zoneObj = new JsonObject();
            zoneObj.addProperty("id", zone.zoneId);
            zoneObj.addProperty("centerX", zone.centerX);
            zoneObj.addProperty("centerZ", zone.centerZ);
            zoneObj.addProperty("radius", zone.radius);
            zoneObj.addProperty("maxRadius", zone.maxRadius);
            zoneObj.addProperty("tier", zone.tier);
            zoneObj.addProperty("corruptionLevel", zone.corruptionLevel);
            zoneObj.addProperty("sourceType", zone.sourceType);
            zoneObj.addProperty("active", zone.active);
            zoneObj.addProperty("chunksAffected", zone.getAffectedChunkCount());
            zoneObj.addProperty("age", formatAge(level.getServer().getTickCount() - zone.createdTick));
            // Backward compatibility
            zoneObj.addProperty("strength", zone.tier);
            zoneObj.addProperty("source", zone.sourceType.toUpperCase());
            zonesArr.add(zoneObj);
        }
        root.add("zones", zonesArr);

        // Totals
        root.addProperty("totalZones", cm.getActiveZoneCount());
        root.addProperty("totalCorruptedChunks", cm.getTotalCorruptedChunks());
        root.addProperty("spreadEnabled", cm.isSpreadEnabled());
        root.addProperty("maxActiveZones", cm.getMaxActiveZones());

        // Active purge info
        if (pm.hasPurgeActive()) {
            PurgeManager.PurgeEvent purge = pm.getActivePurge();
            JsonObject purgeObj = new JsonObject();
            purgeObj.addProperty("purgeId", purge.purgeId);
            purgeObj.addProperty("zoneId", purge.targetZoneId);
            purgeObj.addProperty("progress", purge.currentKills + "/" + purge.killsRequired);
            long ticksLeft = purge.getTimeRemainingTicks(level.getServer().getTickCount());
            purgeObj.addProperty("timeLeft", formatTime(ticksLeft));
            purgeObj.addProperty("participants", purge.participants.size());
            purgeObj.addProperty("initiator", purge.initiatorUuid.toString());
            root.add("activePurge", purgeObj);
        }

        // Stats
        JsonObject stats = new JsonObject();
        stats.addProperty("zonesCreated", cm.getTotalZonesCreated());
        stats.addProperty("zonesDestroyed", cm.getTotalZonesDestroyed());
        stats.addProperty("purgesCompleted", cm.getTotalPurgesCompleted());
        stats.addProperty("purgesFailed", cm.getTotalPurgesFailed());
        root.add("stats", stats);

        sendResponse(player, "corruption_data", root.toString(), eco);
    }

    private static void handleCreateZone(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject data = JsonParser.parseString(jsonData).getAsJsonObject();
            long x = data.has("x") ? data.get("x").getAsLong() : (long) player.getX();
            long z = data.has("z") ? data.get("z").getAsLong() : (long) player.getZ();
            int tier = data.has("tier") ? data.get("tier").getAsInt() : 1;

            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            CorruptionZone zone = cm.createZone(x, z, tier, "admin", level.getServer().getTickCount());

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Created corruption zone #" + zone.zoneId + " at (" + x + ", " + z + ") tier " + tier);
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void handleCreateZoneLegacy(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject data = JsonParser.parseString(jsonData).getAsJsonObject();
            long chunkX = data.has("chunkX") ? data.get("chunkX").getAsLong() : (long)(player.getX()) >> 4;
            long chunkZ = data.has("chunkZ") ? data.get("chunkZ").getAsLong() : (long)(player.getZ()) >> 4;
            int strength = data.has("strength") ? data.get("strength").getAsInt() : 3;
            int maxRadius = data.has("maxRadius") ? data.get("maxRadius").getAsInt() : 8;

            // Convert chunk coords to block coords, strength to tier
            long blockX = chunkX * 16 + 8;
            long blockZ = chunkZ * 16 + 8;
            int tier = Math.max(1, Math.min(4, (strength + 1) / 3)); // 1-3->1, 4-6->2, 7-9->3, 10->4

            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            CorruptionZone zone = cm.createZone(blockX, blockZ, tier, "admin", level.getServer().getTickCount());

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Created corruption zone #" + zone.zoneId + " at (" + blockX + ", " + blockZ + ") tier " + tier);
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void handleRemoveZone(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            int zoneId = Integer.parseInt(jsonData.trim());
            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            boolean removed = cm.removeZone(zoneId);

            JsonObject result = new JsonObject();
            result.addProperty("success", removed);
            result.addProperty("message", removed ? "Removed corruption zone #" + zoneId : "Zone #" + zoneId + " not found");
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void handleStartPurge(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            int zoneId = Integer.parseInt(jsonData.trim());
            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            PurgeManager pm = PurgeManager.get(overworld);

            // Check if purge already active
            if (pm.hasPurgeActive()) {
                JsonObject result = new JsonObject();
                result.addProperty("success", false);
                result.addProperty("message", "A purge is already active");
                sendResponse(player, "corruption_result", result.toString(), eco);
                return;
            }

            // Check if player is within 64 blocks of the zone (unless admin)
            CorruptionZone zone = cm.getZone(zoneId);
            if (zone == null || !zone.active) {
                JsonObject result = new JsonObject();
                result.addProperty("success", false);
                result.addProperty("message", "Zone #" + zoneId + " not found or inactive");
                sendResponse(player, "corruption_result", result.toString(), eco);
                return;
            }

            if (!AdminSystem.isAdmin(player)) {
                double dx = player.getX() - zone.centerX;
                double dz = player.getZ() - zone.centerZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 64 + zone.radius) {
                    JsonObject result = new JsonObject();
                    result.addProperty("success", false);
                    result.addProperty("message", "You must be within 64 blocks of the corruption zone to start a purge");
                    sendResponse(player, "corruption_result", result.toString(), eco);
                    return;
                }
            }

            PurgeManager.PurgeEvent purge = pm.startPurge(zoneId, player);

            JsonObject result = new JsonObject();
            result.addProperty("success", purge != null);
            result.addProperty("message", purge != null
                    ? "Purge started for zone #" + zoneId + "! Kill " + purge.killsRequired + " corrupted mobs in 5 minutes!"
                    : "Failed to start purge.");
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void sendPurgeStatus(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        PurgeManager pm = PurgeManager.get(overworld);

        JsonObject root = new JsonObject();
        if (pm.hasPurgeActive()) {
            PurgeManager.PurgeEvent purge = pm.getActivePurge();
            root.addProperty("active", true);
            root.addProperty("purgeId", purge.purgeId);
            root.addProperty("zoneId", purge.targetZoneId);
            root.addProperty("kills", purge.currentKills);
            root.addProperty("killsRequired", purge.killsRequired);
            long ticksLeft = purge.getTimeRemainingTicks(level.getServer().getTickCount());
            root.addProperty("timeLeft", formatTime(ticksLeft));
            root.addProperty("participants", purge.participants.size());
        } else {
            root.addProperty("active", false);
        }

        sendResponse(player, "purge_data", root.toString(), eco);
    }

    private static void handleSetTier(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject data = JsonParser.parseString(jsonData).getAsJsonObject();
            int zoneId = data.get("id").getAsInt();
            int tier = data.get("tier").getAsInt();

            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            CorruptionZone zone = cm.getZone(zoneId);

            if (zone == null) {
                sendError(player, "Zone #" + zoneId + " not found", eco);
                return;
            }

            cm.setZoneTier(zoneId, tier);

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Set zone #" + zoneId + " to tier " + tier);
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void handleToggleSpread(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        CorruptionManager cm = CorruptionManager.get(overworld);
        boolean newState = !cm.isSpreadEnabled();
        cm.setSpreadEnabled(newState);

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("message", "Corruption spread " + (newState ? "enabled" : "disabled"));
        sendResponse(player, "corruption_result", result.toString(), eco);
    }

    private static void handlePurgeStop(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        PurgeManager pm = PurgeManager.get(overworld);

        if (!pm.hasPurgeActive()) {
            JsonObject result = new JsonObject();
            result.addProperty("success", false);
            result.addProperty("message", "No active purge to stop");
            sendResponse(player, "corruption_result", result.toString(), eco);
            return;
        }

        pm.stopPurge(overworld);

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("message", "Purge stopped by admin");
        sendResponse(player, "corruption_result", result.toString(), eco);
    }

    private static void handleModify(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject data = JsonParser.parseString(jsonData).getAsJsonObject();
            int zoneId = data.get("id").getAsInt();
            ServerLevel overworld = level.getServer().overworld();
            CorruptionManager cm = CorruptionManager.get(overworld);
            CorruptionZone zone = cm.getZone(zoneId);

            if (zone == null) {
                sendError(player, "Zone #" + zoneId + " not found", eco);
                return;
            }

            if (data.has("tier")) {
                cm.setZoneTier(zoneId, data.get("tier").getAsInt());
            }
            if (data.has("strength")) {
                // Legacy: map strength to tier
                cm.setZoneTier(zoneId, Math.max(1, Math.min(4, (data.get("strength").getAsInt() + 1) / 3)));
            }
            if (data.has("radius")) {
                cm.setZoneRadius(zoneId, data.get("radius").getAsInt());
            }

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Modified corruption zone #" + zoneId);
            sendResponse(player, "corruption_result", result.toString(), eco);
        } catch (Exception e) {
            sendError(player, e.getMessage(), eco);
        }
    }

    private static void handleClearAll(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        ServerLevel overworld = level.getServer().overworld();
        CorruptionManager cm = CorruptionManager.get(overworld);
        int count = cm.getActiveZoneCount();
        cm.clearAll();

        PurgeManager pm = PurgeManager.get(overworld);
        if (pm.hasPurgeActive()) {
            pm.stopPurge(overworld);
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("message", "Cleared " + count + " corruption zones");
        sendResponse(player, "corruption_result", result.toString(), eco);
    }

    // ---- Utility ----

    private static String formatAge(long ticks) {
        if (ticks < 0) ticks = 0;
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long days = hours / 24;
        hours = hours % 24;
        long minutes = (totalSeconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    private static String formatTime(long ticks) {
        if (ticks < 0) ticks = 0;
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes + "m " + seconds + "s";
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    private static void sendError(ServerPlayer player, String message, EconomyManager eco) {
        JsonObject result = new JsonObject();
        result.addProperty("success", false);
        result.addProperty("message", "Error: " + message);
        sendResponse(player, "corruption_result", result.toString(), eco);
    }
}
