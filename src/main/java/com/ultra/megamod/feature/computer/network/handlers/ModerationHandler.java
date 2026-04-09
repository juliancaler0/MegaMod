package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.moderation.ModerationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModerationHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) return false;

        ModerationManager mod = ModerationManager.get(level);
        String adminName = player.getGameProfile().name();

        switch (action) {
            case "mod_request_data": {
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_ban": {
                // jsonData = "playerName:reason:durationMinutes"
                String[] parts = jsonData.split(":", 3);
                if (parts.length < 3) {
                    sendResponse(player, "mod_action_result", "{\"success\":false,\"msg\":\"Invalid ban data\"}", eco);
                    return true;
                }
                String targetName = parts[0].trim();
                String reason = parts[1].trim();
                int durationMinutes;
                try {
                    durationMinutes = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    durationMinutes = 0;
                }

                if (reason.isEmpty()) reason = "Banned by admin";

                if (durationMinutes <= 0) {
                    // Permanent ban via vanilla command
                    AdminSystem.executeCommand(player, "ban " + targetName + " " + reason);
                    mod.addTimedBan(targetName, resolveUUID(targetName, level), reason, 0, adminName);
                    mod.logAction("BAN", adminName, targetName, "Permanent: " + reason);
                } else {
                    // Timed ban - ban via vanilla, track expiry ourselves
                    AdminSystem.executeCommand(player, "ban " + targetName + " " + reason);
                    long durationMs = (long) durationMinutes * 60L * 1000L;
                    mod.addTimedBan(targetName, resolveUUID(targetName, level), reason, durationMs, adminName);
                    mod.logAction("BAN", adminName, targetName, ModerationManager.formatDuration(durationMs) + ": " + reason);
                }

                // Kick the player if online
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target != null) {
                    target.connection.disconnect((Component) Component.literal("Banned: " + reason));
                }

                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Banned " + escapeJson(targetName) + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_unban": {
                String targetName = jsonData.trim();
                AdminSystem.executeCommand(player, "pardon " + targetName);
                mod.removeTimedBan(targetName);
                mod.logAction("UNBAN", adminName, targetName, "Unbanned by admin");
                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Unbanned " + escapeJson(targetName) + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_mute": {
                // jsonData = "playerName:durationMinutes"
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 2) {
                    sendResponse(player, "mod_action_result", "{\"success\":false,\"msg\":\"Invalid mute data\"}", eco);
                    return true;
                }
                String targetName = parts[0].trim();
                int durationMinutes;
                try {
                    durationMinutes = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    durationMinutes = 0;
                }

                long durationMs = durationMinutes <= 0 ? 0 : (long) durationMinutes * 60L * 1000L;
                String uuid = resolveUUID(targetName, level);
                mod.mutePlayer(targetName, uuid, "Muted by admin", durationMs);

                // Also set vanilla mute via AdminSystem for chat blocking
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target != null) {
                    AdminSystem.mute(target.getUUID());
                }

                String durStr = durationMinutes <= 0 ? "permanently" : "for " + ModerationManager.formatDuration(durationMs);
                mod.logAction("MUTE", adminName, targetName, "Muted " + durStr);
                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Muted " + escapeJson(targetName) + " " + durStr + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_unmute": {
                String targetName = jsonData.trim();
                mod.unmutePlayer(targetName);

                // Also remove vanilla mute
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target != null) {
                    AdminSystem.unmute(target.getUUID());
                }

                mod.logAction("UNMUTE", adminName, targetName, "Unmuted by admin");
                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Unmuted " + escapeJson(targetName) + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_warn": {
                // jsonData = "playerName:reason"
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 2) {
                    sendResponse(player, "mod_action_result", "{\"success\":false,\"msg\":\"Invalid warn data\"}", eco);
                    return true;
                }
                String targetName = parts[0].trim();
                String reason = parts[1].trim();
                if (reason.isEmpty()) reason = "Warned by admin";

                String uuid = resolveUUID(targetName, level);
                mod.warnPlayer(targetName, uuid, reason, adminName);
                mod.logAction("WARN", adminName, targetName, reason);

                // Notify the target if online
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target != null) {
                    ModerationManager.WarnData wData = mod.getWarnings(targetName);
                    int count = wData != null ? wData.warnings().size() : 1;
                    target.sendSystemMessage((Component) Component.literal("\u00a7c\u00a7l[WARNING] \u00a7e" + reason + " \u00a77(Warning #" + count + ")"));
                }

                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Warned " + escapeJson(targetName) + ": " + escapeJson(reason) + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_clear_warnings": {
                String targetName = jsonData.trim();
                mod.clearWarnings(targetName);
                mod.logAction("CLEAR_WARNINGS", adminName, targetName, "Cleared all warnings");
                mod.saveToDisk(level);
                sendResponse(player, "mod_action_result", "{\"success\":true,\"msg\":\"Cleared warnings for " + escapeJson(targetName) + "\"}", eco);
                sendModerationData(player, level, mod, eco);
                return true;
            }
            case "mod_request_log": {
                sendLogData(player, mod, eco, "All");
                return true;
            }
            case "mod_request_log_filtered": {
                sendLogData(player, mod, eco, jsonData.trim());
                return true;
            }
            default:
                return false;
        }
    }

    private static void sendModerationData(ServerPlayer player, ServerLevel level, ModerationManager mod, EconomyManager eco) {
        StringBuilder sb = new StringBuilder("{");

        // Bans - combine vanilla ban list with our timed ban data
        sb.append("\"bans\":[");
        List<ModerationManager.TimedBan> timedBans = mod.getActiveBans();
        UserBanList vanillaBans = level.getServer().getPlayerList().getBans();
        boolean first = true;

        // Add all entries from our timed ban list (which tracks both permanent and timed)
        for (ModerationManager.TimedBan ban : timedBans) {
            if (!first) sb.append(",");
            first = false;
            boolean permanent = ban.expireTime() <= 0;
            String duration;
            if (permanent) {
                duration = "Permanent";
            } else {
                long remaining = ban.expireTime() - System.currentTimeMillis();
                duration = remaining > 0 ? ModerationManager.formatDuration(remaining) : "Expired";
            }
            sb.append("{\"name\":\"").append(escapeJson(ban.playerName())).append("\"");
            sb.append(",\"uuid\":\"").append(escapeJson(ban.uuid())).append("\"");
            sb.append(",\"reason\":\"").append(escapeJson(ban.reason())).append("\"");
            sb.append(",\"date\":\"").append(ModerationManager.formatDate(ban.banTime())).append("\"");
            sb.append(",\"duration\":\"").append(duration).append("\"");
            sb.append(",\"bannedBy\":\"").append(escapeJson(ban.bannedBy())).append("\"");
            sb.append(",\"permanent\":").append(permanent).append("}");
        }
        sb.append("]");

        // Mutes
        sb.append(",\"mutes\":[");
        List<ModerationManager.MuteData> activeMutes = mod.getActiveMutes();
        first = true;
        for (ModerationManager.MuteData mute : activeMutes) {
            if (!first) sb.append(",");
            first = false;
            String duration;
            if (mute.expireTime() <= 0) {
                duration = "Permanent";
            } else {
                long remaining = mute.expireTime() - System.currentTimeMillis();
                duration = remaining > 0 ? ModerationManager.formatDuration(remaining) + " remaining" : "Expired";
            }
            sb.append("{\"name\":\"").append(escapeJson(mute.playerName())).append("\"");
            sb.append(",\"uuid\":\"").append(escapeJson(mute.uuid())).append("\"");
            sb.append(",\"reason\":\"").append(escapeJson(mute.reason())).append("\"");
            sb.append(",\"expiresAt\":").append(mute.expireTime());
            sb.append(",\"duration\":\"").append(duration).append("\"}");
        }
        sb.append("]");

        // Warnings
        sb.append(",\"warnings\":[");
        Map<String, ModerationManager.WarnData> allWarnings = mod.getAllWarnings();
        first = true;
        for (ModerationManager.WarnData wd : allWarnings.values()) {
            if (!first) sb.append(",");
            first = false;
            List<ModerationManager.Warning> warnList = wd.warnings();
            String lastReason = "";
            String lastDate = "";
            if (!warnList.isEmpty()) {
                ModerationManager.Warning last = warnList.get(warnList.size() - 1);
                lastReason = last.reason();
                lastDate = ModerationManager.formatDate(last.timestamp());
            }
            sb.append("{\"name\":\"").append(escapeJson(wd.playerName())).append("\"");
            sb.append(",\"uuid\":\"").append(escapeJson(wd.uuid())).append("\"");
            sb.append(",\"count\":").append(warnList.size());
            sb.append(",\"lastReason\":\"").append(escapeJson(lastReason)).append("\"");
            sb.append(",\"lastDate\":\"").append(lastDate).append("\"}");
        }
        sb.append("]");

        sb.append("}");
        sendResponse(player, "mod_data", sb.toString(), eco);
    }

    private static void sendLogData(ServerPlayer player, ModerationManager mod, EconomyManager eco, String filter) {
        List<ModerationManager.ActionLog> log = mod.getActionLog();
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (int i = log.size() - 1; i >= 0; i--) {
            ModerationManager.ActionLog entry = log.get(i);

            // Apply filter
            if (!"All".equals(filter)) {
                String type = entry.actionType().toUpperCase();
                boolean match = switch (filter) {
                    case "Bans" -> type.contains("BAN");
                    case "Kicks" -> type.equals("KICK");
                    case "Mutes" -> type.contains("MUTE");
                    case "Warns" -> type.contains("WARN");
                    default -> true;
                };
                if (!match) continue;
            }

            if (!first) sb.append(",");
            first = false;
            sb.append("{\"timestamp\":\"").append(ModerationManager.formatTimestamp(entry.timestamp())).append("\"");
            sb.append(",\"action\":\"").append(escapeJson(entry.actionType())).append("\"");
            sb.append(",\"admin\":\"").append(escapeJson(entry.adminName())).append("\"");
            sb.append(",\"target\":\"").append(escapeJson(entry.targetName())).append("\"");
            sb.append(",\"details\":\"").append(escapeJson(entry.details())).append("\"");
            sb.append(",\"type\":\"").append(escapeJson(entry.actionType())).append("\"}");
        }
        sb.append("]");
        sendResponse(player, "mod_log_data", sb.toString(), eco);
    }

    private static String resolveUUID(String playerName, ServerLevel level) {
        ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(playerName);
        if (target != null) {
            return target.getUUID().toString();
        }
        return "";
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer((ServerPlayer) player, (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
