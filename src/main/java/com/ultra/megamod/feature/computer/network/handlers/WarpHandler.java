package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.admin.AdminWarpManager;
import com.ultra.megamod.feature.computer.admin.AdminWarpManager.WarpPoint;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Set;

public class WarpHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "warp_request": {
                if (!checkAdmin(player, eco)) return true;
                sendWarpData(player, level, eco);
                return true;
            }

            case "warp_save": {
                if (!checkAdmin(player, eco)) return true;
                String name = jsonData.trim();
                if (name.isEmpty()) {
                    sendResult(player, eco, false, "Warp name cannot be empty.");
                    return true;
                }
                AdminWarpManager mgr = AdminWarpManager.get(level);
                mgr.saveWarp(name, player);
                mgr.saveToDisk(level);
                // Send updated warp list so Warps tab refreshes immediately
                sendWarpData(player, level, eco);
                return true;
            }

            case "warp_delete": {
                if (!checkAdmin(player, eco)) return true;
                String name = jsonData.trim();
                AdminWarpManager mgr = AdminWarpManager.get(level);
                if (mgr.getWarp(name) == null) {
                    sendResult(player, eco, false, "Warp not found: " + name);
                    return true;
                }
                mgr.deleteWarp(name);
                mgr.saveToDisk(level);
                // Send updated warp list so Warps tab refreshes immediately
                sendWarpData(player, level, eco);
                return true;
            }

            case "warp_goto": {
                if (!checkAdmin(player, eco)) return true;
                String name = jsonData.trim();
                AdminWarpManager mgr = AdminWarpManager.get(level);
                WarpPoint wp = mgr.getWarp(name);
                if (wp == null) {
                    sendResult(player, eco, false, "Warp not found: " + name);
                    return true;
                }
                String currentDim = player.level().dimension().identifier().toString();
                if (!wp.dimension().equals(currentDim)) {
                    ResourceKey<Level> dimKey = ResourceKey.create(
                            (ResourceKey) Registries.DIMENSION,
                            (Identifier) Identifier.parse(wp.dimension()));
                    ServerLevel targetLevel = player.level().getServer().getLevel(dimKey);
                    if (targetLevel == null) {
                        sendResult(player, eco, false, "Dimension not found: " + wp.dimension());
                        return true;
                    }
                    player.teleportTo(targetLevel, wp.x(), wp.y(), wp.z(), Set.of(), wp.yRot(), wp.xRot(), false);
                } else {
                    player.teleportTo(level, wp.x(), wp.y(), wp.z(), Set.of(), wp.yRot(), wp.xRot(), false);
                }
                sendResult(player, eco, true, "Warped to: " + name);
                return true;
            }

            default:
                return false;
        }
    }

    private static boolean checkAdmin(ServerPlayer player, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            sendResult(player, eco, false, "Admin access required.");
            return false;
        }
        return true;
    }

    private static void sendWarpData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        AdminWarpManager mgr = AdminWarpManager.get(level);
        Map<String, WarpPoint> warps = mgr.getAllWarps();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"warps\":[");
        int i = 0;
        for (Map.Entry<String, WarpPoint> entry : warps.entrySet()) {
            if (i > 0) sb.append(",");
            WarpPoint wp = entry.getValue();
            sb.append("{\"name\":\"").append(escapeJson(entry.getKey())).append("\"");
            sb.append(",\"x\":").append((int) wp.x());
            sb.append(",\"y\":").append((int) wp.y());
            sb.append(",\"z\":").append((int) wp.z());
            sb.append(",\"dimension\":\"").append(escapeJson(wp.dimension())).append("\"");
            sb.append("}");
            i++;
        }
        sb.append("]}");

        sendResponse(player, "warp_data", sb.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, EconomyManager eco, boolean success, String message) {
        String json = "{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}";
        sendResponse(player, "warp_result", json, eco);
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
