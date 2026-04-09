package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.baritone.BotInstance;
import com.ultra.megamod.feature.baritone.BotManager;
import com.ultra.megamod.feature.baritone.process.BotProcess;
import com.ultra.megamod.feature.baritone.process.WaypointManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;

/**
 * Server-side handler for bot control commands from the Admin Computer Panel.
 */
public class BotControlHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "bot_request_status" -> {
                sendBotStatus(player, level, eco);
                return true;
            }
            case "bot_command" -> {
                handleCommand(player, jsonData, level, eco);
                return true;
            }
            case "bot_toggle_setting" -> {
                handleToggleSetting(player, jsonData, level, eco);
                return true;
            }
            case "bot_cancel" -> {
                handleCancel(player, jsonData, level, eco);
                return true;
            }
            case "bot_request_log" -> {
                handleLogRequest(player, jsonData, level, eco);
                return true;
            }
            case "bot_request_path" -> {
                handlePathRequest(player, jsonData, level, eco);
                return true;
            }
            case "bot_waypoint_save" -> {
                handleWaypointSave(player, jsonData, level, eco);
                return true;
            }
            case "bot_waypoint_delete" -> {
                handleWaypointDelete(player, jsonData, level, eco);
                return true;
            }
            case "bot_waypoint_list" -> {
                handleWaypointList(player, jsonData, level, eco);
                return true;
            }
            case "bot_group_command" -> {
                handleGroupCommand(player, jsonData, level, eco);
                return true;
            }
            case "bot_settings_all" -> {
                handleSettingsAll(player, jsonData, level, eco);
                return true;
            }
            case "bot_stats" -> {
                handleStats(player, jsonData, level, eco);
                return true;
            }
        }
        return false;
    }

    private static void sendBotStatus(ServerPlayer admin, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();
        JsonArray playersArr = new JsonArray();

        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("name", p.getGameProfile().name());
            pObj.addProperty("uuid", p.getUUID().toString());
            pObj.addProperty("x", p.blockPosition().getX());
            pObj.addProperty("y", p.blockPosition().getY());
            pObj.addProperty("z", p.blockPosition().getZ());

            BotInstance bot = BotManager.get(p.getUUID());
            if (bot != null) {
                pObj.addProperty("botActive", true);
                pObj.addProperty("botStatus", bot.getProcessManager().getStatus());
                pObj.addProperty("botPaused", bot.isPaused());
                pObj.addProperty("settings", bot.getSettings().toJson());
                pObj.addProperty("blocksMined", bot.getBlocksMined());
                pObj.addProperty("cropsHarvested", bot.getCropsHarvested());
                pObj.addProperty("blocksPlaced", bot.getBlocksPlaced());

                // Extended fields for enhanced panel
                pObj.addProperty("kills", bot.getKills());
                BotProcess activeProcess = bot.getProcessManager().getActiveProcess();
                pObj.addProperty("processName", activeProcess != null ? activeProcess.name() : "none");
                pObj.addProperty("eta", bot.getEtaSeconds());

                // Goal coordinates
                int[] goalCoords = bot.getGoalCoords();
                if (goalCoords != null) {
                    pObj.addProperty("goalX", goalCoords[0]);
                    pObj.addProperty("goalY", goalCoords[1]);
                    pObj.addProperty("goalZ", goalCoords[2]);
                }
            } else {
                pObj.addProperty("botActive", false);
            }
            playersArr.add(pObj);
        }
        root.add("players", playersArr);

        root.addProperty("activeBots", (int) BotManager.getAllBots().values().stream()
            .filter(b -> !b.isPaused() && b.getProcessManager().getActiveProcess() != null)
            .count());
        root.addProperty("totalBots", BotManager.getAllBots().size());

        sendResponse(admin, "bot_status", root.toString(), eco);
    }

    private static void handleCommand(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();
            String cmd = obj.get("cmd").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            BotInstance bot = BotManager.getOrCreate(target);
            String result = bot.executeCommand(cmd, (ServerLevel) target.level());
            sendResult(admin, result, eco);
            sendBotStatus(admin, level, eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleToggleSetting(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();
            String key = obj.get("key").getAsString();
            String value = obj.get("value").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            BotInstance bot = BotManager.getOrCreate(target);
            bot.getSettings().set(key, value);
            sendResult(admin, "Set " + key + " = " + value + " for " + targetName, eco);
            sendBotStatus(admin, level, eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleCancel(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            BotInstance bot = BotManager.get(target.getUUID());
            if (bot != null) {
                bot.executeCommand("stop", (ServerLevel) target.level());
                BotManager.remove(target.getUUID());
                sendResult(admin, "Bot removed for " + targetName, eco);
            } else {
                sendResult(admin, "No bot active for " + targetName, eco);
            }
            sendBotStatus(admin, level, eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleLogRequest(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResponse(admin, "bot_log", "{\"log\":[]}", eco);
                return;
            }

            BotInstance bot = BotManager.get(target.getUUID());
            JsonObject result = new JsonObject();
            JsonArray logArr = new JsonArray();
            if (bot != null) {
                List<String> snapshot = bot.getActionLog();
                int start = Math.max(0, snapshot.size() - 50);
                for (int i = start; i < snapshot.size(); i++) {
                    logArr.add(snapshot.get(i));
                }
            }
            result.add("log", logArr);
            result.addProperty("player", targetName);
            sendResponse(admin, "bot_log", result.toString(), eco);
        } catch (Exception e) {
            sendResponse(admin, "bot_log", "{\"log\":[]}", eco);
        }
    }

    private static void handlePathRequest(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResponse(admin, "bot_path", "{\"path\":[]}", eco);
                return;
            }

            BotInstance bot = BotManager.get(target.getUUID());
            if (bot != null) {
                sendResponse(admin, "bot_path", bot.getPathJson(), eco);
            } else {
                sendResponse(admin, "bot_path", "{\"path\":[]}", eco);
            }
        } catch (Exception e) {
            sendResponse(admin, "bot_path", "{\"path\":[]}", eco);
        }
    }

    private static void handleWaypointSave(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();
            String name = obj.get("name").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            WaypointManager.save(name, target.getUUID(), target.blockPosition());
            sendResult(admin, "Saved waypoint '" + name + "' for " + targetName, eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleWaypointDelete(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();
            String name = obj.get("name").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            if (WaypointManager.delete(name, target.getUUID())) {
                sendResult(admin, "Deleted waypoint '" + name + "'", eco);
            } else {
                sendResult(admin, "Waypoint not found: " + name, eco);
            }
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleWaypointList(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResponse(admin, "bot_waypoints", "{\"waypoints\":[]}", eco);
                return;
            }

            JsonObject result = new JsonObject();
            JsonArray wpArr = new JsonArray();
            Map<String, BlockPos> wps = WaypointManager.list(target.getUUID());
            for (Map.Entry<String, BlockPos> entry : wps.entrySet()) {
                JsonObject wp = new JsonObject();
                wp.addProperty("name", entry.getKey());
                wp.addProperty("x", entry.getValue().getX());
                wp.addProperty("y", entry.getValue().getY());
                wp.addProperty("z", entry.getValue().getZ());
                wpArr.add(wp);
            }
            result.add("waypoints", wpArr);
            result.addProperty("player", targetName);
            sendResponse(admin, "bot_waypoints", result.toString(), eco);
        } catch (Exception e) {
            sendResponse(admin, "bot_waypoints", "{\"waypoints\":[]}", eco);
        }
    }

    private static void handleGroupCommand(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String cmd = obj.get("cmd").getAsString();

            int count = 0;
            for (BotInstance bot : BotManager.getAllBots().values()) {
                if (!bot.getPlayer().isRemoved()) {
                    bot.executeCommand(cmd, (ServerLevel) bot.getPlayer().level());
                    count++;
                }
            }
            sendResult(admin, "Sent '" + cmd + "' to " + count + " bots", eco);
            sendBotStatus(admin, level, eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleSettingsAll(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            BotInstance bot = BotManager.getOrCreate(target);
            JsonObject result = new JsonObject();
            result.addProperty("settings", bot.getSettings().toJson());
            result.addProperty("player", targetName);
            sendResponse(admin, "bot_settings_all", result.toString(), eco);
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void handleStats(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String targetName = obj.get("target").getAsString();

            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sendResult(admin, "Player not found: " + targetName, eco);
                return;
            }

            BotInstance bot = BotManager.get(target.getUUID());
            if (bot != null) {
                JsonObject result = new JsonObject();
                result.addProperty("player", targetName);
                result.addProperty("blocksMined", bot.getBlocksMined());
                result.addProperty("cropsHarvested", bot.getCropsHarvested());
                result.addProperty("blocksPlaced", bot.getBlocksPlaced());
                result.addProperty("status", bot.getProcessManager().getStatus());
                sendResponse(admin, "bot_stats", result.toString(), eco);
            } else {
                sendResult(admin, "No bot active for " + targetName, eco);
            }
        } catch (Exception e) {
            sendResult(admin, "Error: " + e.getMessage(), eco);
        }
    }

    private static void sendResult(ServerPlayer player, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("msg", message);
        sendResponse(player, "bot_command_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String jsonData, EconomyManager eco) {
        ComputerDataPayload response = new ComputerDataPayload(
            type, jsonData,
            eco.getWallet(player.getUUID()),
            eco.getBank(player.getUUID())
        );
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) response, (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
