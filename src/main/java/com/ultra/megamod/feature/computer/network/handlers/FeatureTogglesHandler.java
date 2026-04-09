package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class FeatureTogglesHandler {

    /**
     * Handles feature toggle actions from the admin computer panel.
     * Returns true if the action was handled, false otherwise.
     */
    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "feature_toggles_request": {
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                String json = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json, eco);
                return true;
            }
            case "feature_toggle_set": {
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                // jsonData format: "featureId:true" or "featureId:false"
                int colonIdx = jsonData.lastIndexOf(':');
                if (colonIdx > 0 && colonIdx < jsonData.length() - 1) {
                    String featureId = jsonData.substring(0, colonIdx);
                    boolean enabled = Boolean.parseBoolean(jsonData.substring(colonIdx + 1));
                    mgr.setEnabled(featureId, enabled);
                    mgr.saveToDisk(level);
                    // Sync specific toggles to client-side systems
                    if ("dungeon_loot_glow".equals(featureId)) {
                        com.ultra.megamod.feature.dungeons.DungeonLootGlow.enabled = enabled;
                    }
                }
                // Send back updated data
                String json = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json, eco);
                return true;
            }
            case "feature_toggles_enable_all": {
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                mgr.enableAll();
                mgr.saveToDisk(level);
                String json = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json, eco);
                return true;
            }
            case "feature_toggles_disable_all": {
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                mgr.disableAll();
                mgr.saveToDisk(level);
                String json = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json, eco);
                return true;
            }
            case "feature_toggles_reset": {
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                mgr.resetDefaults();
                mgr.saveToDisk(level);
                String json = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json, eco);
                return true;
            }
            case "feature_numeric_set": {
                // jsonData format: "key:value" e.g. "builder_speed_multiplier:5"
                FeatureToggleManager mgr = FeatureToggleManager.get(level);
                int colonIdx2 = jsonData.lastIndexOf(':');
                if (colonIdx2 > 0 && colonIdx2 < jsonData.length() - 1) {
                    String key = jsonData.substring(0, colonIdx2);
                    try {
                        int value = Integer.parseInt(jsonData.substring(colonIdx2 + 1));
                        mgr.setNumericSetting(key, value);
                        mgr.saveToDisk(level);
                    } catch (NumberFormatException ignored) {}
                }
                String json3 = buildFeatureListJson(mgr);
                sendResponse(player, "feature_toggles_data", json3, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static String buildFeatureListJson(FeatureToggleManager mgr) {
        Map<String, Boolean> toggles = mgr.getAllToggles();
        List<FeatureToggleManager.FeatureDefinition> allFeatures = FeatureToggleManager.ALL_FEATURES;
        int enabledCount = mgr.getEnabledCount();
        int totalCount = mgr.getTotalCount();

        StringBuilder sb = new StringBuilder(2048);
        sb.append("{\"features\":[");
        boolean first = true;
        for (FeatureToggleManager.FeatureDefinition def : allFeatures) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("{\"id\":\"");
            sb.append(escapeJson(def.id()));
            sb.append("\",\"name\":\"");
            sb.append(escapeJson(def.name()));
            sb.append("\",\"category\":\"");
            sb.append(escapeJson(def.category()));
            sb.append("\",\"description\":\"");
            sb.append(escapeJson(def.description()));
            sb.append("\",\"enabled\":");
            sb.append(toggles.getOrDefault(def.id(), true));
            sb.append('}');
        }
        sb.append("],\"enabledCount\":");
        sb.append(enabledCount);
        sb.append(",\"totalCount\":");
        sb.append(totalCount);
        // Include numeric settings
        sb.append(",\"numeric\":{");
        Map<String, Integer> numericSettings = mgr.getAllNumericSettings();
        boolean firstNum = true;
        for (Map.Entry<String, Integer> numEntry : numericSettings.entrySet()) {
            if (!firstNum) sb.append(',');
            firstNum = false;
            sb.append("\"").append(escapeJson(numEntry.getKey())).append("\":");
            sb.append(numEntry.getValue());
        }
        sb.append('}');
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}
