package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.encyclopedia.DiscoveryManager;
import com.ultra.megamod.feature.museum.MuseumData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;

public class EncyclopediaHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "encyclopedia_request": {
                // Wiki is fully unlocked for all players
                DiscoveryManager dm = DiscoveryManager.get(level);
                dm.discoverAll(player.getUUID());
                dm.saveToDisk(level);
                // Send allUnlocked flag so client treats ALL entries as discovered
                sendResponse(player, "encyclopedia_data", "{\"allUnlocked\":true,\"discoveries\":[]}", eco);
                return true;
            }
            case "encyclopedia_unlock_all": {
                if (!AdminSystem.isAdmin(player)) {
                    sendResponse(player, "encyclopedia_result", "{\"msg\":\"Access denied\"}", eco);
                    return true;
                }
                DiscoveryManager dm = DiscoveryManager.get(level);
                UUID target;
                if (jsonData == null || jsonData.isEmpty()) {
                    target = player.getUUID();
                } else {
                    try {
                        target = UUID.fromString(jsonData.trim());
                    } catch (IllegalArgumentException e) {
                        target = player.getUUID();
                    }
                }
                dm.discoverAll(target);
                dm.saveToDisk(level);
                sendResponse(player, "encyclopedia_data", "{\"allUnlocked\":true,\"discoveries\":[]}", eco);
                return true;
            }
            case "encyclopedia_clear": {
                if (!AdminSystem.isAdmin(player)) {
                    sendResponse(player, "encyclopedia_result", "{\"msg\":\"Access denied\"}", eco);
                    return true;
                }
                DiscoveryManager dm = DiscoveryManager.get(level);
                UUID target;
                if (jsonData == null || jsonData.isEmpty()) {
                    target = player.getUUID();
                } else {
                    try {
                        target = UUID.fromString(jsonData.trim());
                    } catch (IllegalArgumentException e) {
                        target = player.getUUID();
                    }
                }
                dm.clearAll(target);
                dm.saveToDisk(level);
                sendResponse(player, "encyclopedia_data", "{\"discoveries\":[]}", eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}
