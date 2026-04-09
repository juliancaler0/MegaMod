package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.scheduler.CommandScheduler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SchedulerHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        CommandScheduler scheduler = CommandScheduler.get(level);

        switch (action) {
            case "scheduler_request": {
                sendSchedulerData(player, scheduler, eco);
                return true;
            }

            case "scheduler_create": {
                // Format: "name|command|intervalMs|repeat"
                String[] parts = jsonData.split("\\|", 4);
                if (parts.length < 4) {
                    sendResult(player, eco, false, "Invalid format. Expected: name|command|intervalMs|repeat");
                    return true;
                }
                String name = parts[0].trim();
                String command = parts[1].trim();
                long intervalMs;
                try {
                    intervalMs = Long.parseLong(parts[2].trim());
                } catch (NumberFormatException e) {
                    sendResult(player, eco, false, "Invalid interval: " + parts[2]);
                    return true;
                }
                boolean repeat = Boolean.parseBoolean(parts[3].trim());

                if (name.isEmpty() || command.isEmpty()) {
                    sendResult(player, eco, false, "Name and command are required.");
                    return true;
                }
                if (intervalMs < 1000) {
                    sendResult(player, eco, false, "Interval must be at least 1 second.");
                    return true;
                }
                if (scheduler.getTasks().size() >= 50) {
                    sendResult(player, eco, false, "Maximum of 50 schedules reached.");
                    return true;
                }

                String id = scheduler.createTask(name, command, intervalMs, repeat);
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Created schedule: " + name + " (ID: " + id + ")");
                return true;
            }

            case "scheduler_delete": {
                String taskId = jsonData.trim();
                CommandScheduler.ScheduledTask task = findTask(scheduler, taskId);
                if (task == null) {
                    sendResult(player, eco, false, "Schedule not found: " + taskId);
                    return true;
                }
                String taskName = task.name;
                scheduler.deleteTask(taskId);
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Deleted schedule: " + taskName);
                return true;
            }

            case "scheduler_pause": {
                String taskId = jsonData.trim();
                CommandScheduler.ScheduledTask task = findTask(scheduler, taskId);
                if (task == null) {
                    sendResult(player, eco, false, "Schedule not found: " + taskId);
                    return true;
                }
                scheduler.pauseTask(taskId);
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Paused: " + task.name);
                return true;
            }

            case "scheduler_resume": {
                String taskId = jsonData.trim();
                CommandScheduler.ScheduledTask task = findTask(scheduler, taskId);
                if (task == null) {
                    sendResult(player, eco, false, "Schedule not found: " + taskId);
                    return true;
                }
                scheduler.resumeTask(taskId);
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Resumed: " + task.name);
                return true;
            }

            case "scheduler_run_now": {
                String taskId = jsonData.trim();
                CommandScheduler.ScheduledTask task = findTask(scheduler, taskId);
                if (task == null) {
                    sendResult(player, eco, false, "Schedule not found: " + taskId);
                    return true;
                }
                scheduler.runNow(taskId, level.getServer());
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Executed: " + task.name + " (" + task.command + ")");
                return true;
            }

            case "scheduler_add_preset": {
                String presetKey = jsonData.trim();
                String presetId = addPreset(scheduler, presetKey);
                if (presetId == null) {
                    sendResult(player, eco, false, "Unknown preset: " + presetKey);
                    return true;
                }
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Added preset schedule: " + presetKey);
                return true;
            }

            case "scheduler_duplicate": {
                String taskId = jsonData.trim();
                CommandScheduler.ScheduledTask task = findTask(scheduler, taskId);
                if (task == null) {
                    sendResult(player, eco, false, "Schedule not found: " + taskId);
                    return true;
                }
                String newId = scheduler.createTask(task.name + " (copy)", task.command, task.intervalMs, task.repeat);
                scheduler.saveToDisk(level);
                sendResult(player, eco, true, "Duplicated: " + task.name + " -> " + task.name + " (copy)");
                return true;
            }

            default:
                return false;
        }
    }

    private static String addPreset(CommandScheduler scheduler, String key) {
        switch (key) {
            case "auto_save":
                return scheduler.createTask("Auto-Save", "save-all", 300000, true);
            case "day_cycle":
                return scheduler.createTask("Day Cycle", "time set day", 300000, true);
            case "clear_mobs":
                return scheduler.createTask("Clear Mobs", "kill @e[type=!player,type=!item]", 600000, true);
            case "restart_warn":
                return scheduler.createTask("Restart Warning", "say Server restart in 30 minutes!", 21600000, true);
            case "double_xp":
                return scheduler.createTask("Double XP", "say Double XP event is now active!", 3600000, true);
            case "weather_clear":
                return scheduler.createTask("Weather Clear", "weather clear", 1800000, true);
            case "auto_backup":
                return scheduler.createTask("Auto-Backup", "save-all flush", 1800000, true);
            case "mob_cap":
                return scheduler.createTask("Mob Cap", "kill @e[type=!player,distance=..200]", 900000, true);
            case "night_skip":
                return scheduler.createTask("Night Skip", "time set day", 600000, true);
            default:
                return null;
        }
    }

    private static CommandScheduler.ScheduledTask findTask(CommandScheduler scheduler, String id) {
        for (CommandScheduler.ScheduledTask task : scheduler.getTasks()) {
            if (task.id.equals(id)) {
                return task;
            }
        }
        return null;
    }

    private static void sendSchedulerData(ServerPlayer player, CommandScheduler scheduler, EconomyManager eco) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"schedules\":[");

        List<CommandScheduler.ScheduledTask> tasks = scheduler.getTasks();
        long now = System.currentTimeMillis();
        for (int i = 0; i < tasks.size(); i++) {
            CommandScheduler.ScheduledTask task = tasks.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"").append(escapeJson(task.id)).append("\"");
            sb.append(",\"name\":\"").append(escapeJson(task.name)).append("\"");
            sb.append(",\"command\":\"").append(escapeJson(task.command)).append("\"");
            sb.append(",\"intervalMs\":").append(task.intervalMs);
            long nextRunMs = task.active ? Math.max(0, task.nextRunTime - now) : 0;
            sb.append(",\"nextRunMs\":").append(nextRunMs);
            sb.append(",\"runCount\":").append(task.runCount);
            sb.append(",\"active\":").append(task.active);
            sb.append("}");
        }

        sb.append("],\"log\":[");

        List<CommandScheduler.ExecutionLog> log = scheduler.getLog();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        int logLimit = Math.min(log.size(), 100);
        for (int i = 0; i < logLimit; i++) {
            CommandScheduler.ExecutionLog entry = log.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"timestamp\":\"").append(sdf.format(new Date(entry.timestamp()))).append("\"");
            sb.append(",\"name\":\"").append(escapeJson(entry.name())).append("\"");
            sb.append(",\"command\":\"").append(escapeJson(entry.command())).append("\"");
            sb.append(",\"result\":\"").append(escapeJson(entry.result())).append("\"");
            sb.append("}");
        }

        sb.append("]}");

        sendResponse(player, "scheduler_data", sb.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, EconomyManager eco, boolean success, String message) {
        String json = "{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}";
        sendResponse(player, "scheduler_result", json, eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer((ServerPlayer) player, (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
