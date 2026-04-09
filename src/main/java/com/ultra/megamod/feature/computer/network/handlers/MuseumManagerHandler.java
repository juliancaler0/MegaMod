package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.catalog.AchievementCatalog;
import com.ultra.megamod.feature.museum.catalog.AquariumCatalog;
import com.ultra.megamod.feature.museum.catalog.ArtCatalog;
import com.ultra.megamod.feature.museum.catalog.ItemCatalog;
import com.ultra.megamod.feature.museum.catalog.WildlifeCatalog;
import com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MuseumManagerHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "museum_request_data": {
                UUID targetId;
                try {
                    targetId = UUID.fromString(jsonData.trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }

                MuseumData data = MuseumData.get(level);
                String targetName = resolveName(targetId, level);

                Set<String> donatedItems = data.getDonatedItems(targetId);
                Set<String> donatedMobs = data.getDonatedMobs(targetId);
                Set<String> donatedArt = data.getDonatedArt(targetId);
                Set<String> donatedAchievements = data.getCompletedAchievements(targetId);

                // Items wing: count donated items that are in the catalog
                Set<String> allCatalogItems = getAllCatalogItems();
                int itemsTotal = allCatalogItems.size();
                int itemsDonated = 0;
                List<String> missingItems = new ArrayList<>();
                for (String itemId : allCatalogItems) {
                    if (donatedItems.contains(itemId)) {
                        itemsDonated++;
                    } else {
                        missingItems.add(itemId);
                    }
                }

                // Aquarium wing
                int aquariumTotal = AquariumCatalog.getTotalCount();
                int aquariumDonated = 0;
                List<String> missingAquarium = new ArrayList<>();
                for (AquariumCatalog.MobEntry entry : AquariumCatalog.ENTRIES) {
                    if (donatedMobs.contains(entry.entityId())) {
                        aquariumDonated++;
                    } else {
                        missingAquarium.add(entry.entityId());
                    }
                }

                // Wildlife wing
                int wildlifeTotal = WildlifeCatalog.getTotalCount();
                int wildlifeDonated = 0;
                List<String> missingWildlife = new ArrayList<>();
                for (WildlifeCatalog.MobEntry entry : WildlifeCatalog.ENTRIES) {
                    if (donatedMobs.contains(entry.entityId())) {
                        wildlifeDonated++;
                    } else {
                        missingWildlife.add(entry.entityId());
                    }
                }

                // Art wing
                int artTotal = ArtCatalog.getTotalCount();
                int artDonated = 0;
                List<String> missingArtList = new ArrayList<>();
                for (ArtCatalog.ArtEntry entry : ArtCatalog.ENTRIES) {
                    if (donatedArt.contains(entry.id())) {
                        artDonated++;
                    } else {
                        missingArtList.add(entry.id());
                    }
                }

                // Achievements wing
                int achievementsTotal = AchievementCatalog.getTotalCount();
                int achievementsDonated = 0;
                List<String> missingAchievementsList = new ArrayList<>();
                for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
                    if (donatedAchievements.contains(entry.advancementId())) {
                        achievementsDonated++;
                    } else {
                        missingAchievementsList.add(entry.advancementId());
                    }
                }

                // Build JSON response
                StringBuilder sb = new StringBuilder();
                sb.append("{\"playerName\":\"").append(escapeJson(targetName)).append("\"");

                sb.append(",\"items\":{\"donated\":").append(itemsDonated);
                sb.append(",\"total\":").append(itemsTotal);
                sb.append(",\"missing\":").append(toJsonArray(missingItems)).append("}");

                sb.append(",\"aquarium\":{\"donated\":").append(aquariumDonated);
                sb.append(",\"total\":").append(aquariumTotal);
                sb.append(",\"missing\":").append(toJsonArray(missingAquarium)).append("}");

                sb.append(",\"wildlife\":{\"donated\":").append(wildlifeDonated);
                sb.append(",\"total\":").append(wildlifeTotal);
                sb.append(",\"missing\":").append(toJsonArray(missingWildlife)).append("}");

                sb.append(",\"art\":{\"donated\":").append(artDonated);
                sb.append(",\"total\":").append(artTotal);
                sb.append(",\"missing\":").append(toJsonArray(missingArtList)).append("}");

                sb.append(",\"achievements\":{\"donated\":").append(achievementsDonated);
                sb.append(",\"total\":").append(achievementsTotal);
                sb.append(",\"missing\":").append(toJsonArray(missingAchievementsList)).append("}");

                sb.append("}");

                sendResponse(player, "museum_manager_data", sb.toString(), eco);
                return true;
            }

            case "museum_fill_wing": {
                String[] parts = jsonData.split(":", 2);
                if (parts.length != 2) {
                    sendActionResult(player, eco, false, "Invalid format.");
                    return true;
                }
                UUID targetId;
                try {
                    targetId = UUID.fromString(parts[0].trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }
                String wing = parts[1].trim();
                MuseumData data = MuseumData.get(level);
                int filled = fillWing(data, targetId, wing);
                data.saveToDisk(level);
                sendActionResult(player, eco, true, "Filled " + wing + " wing: " + filled + " items added.");
                return true;
            }

            case "museum_clear_wing": {
                String[] parts = jsonData.split(":", 2);
                if (parts.length != 2) {
                    sendActionResult(player, eco, false, "Invalid format.");
                    return true;
                }
                UUID targetId;
                try {
                    targetId = UUID.fromString(parts[0].trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }
                String wing = parts[1].trim();
                MuseumData data = MuseumData.get(level);
                int cleared = clearWing(data, targetId, wing);
                data.saveToDisk(level);
                sendActionResult(player, eco, true, "Cleared " + wing + " wing: " + cleared + " items removed.");
                return true;
            }

            case "museum_fill_all": {
                UUID targetId;
                try {
                    targetId = UUID.fromString(jsonData.trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }
                MuseumData data = MuseumData.get(level);
                int total = 0;
                total += fillWing(data, targetId, "items");
                total += fillWing(data, targetId, "aquarium");
                total += fillWing(data, targetId, "wildlife");
                total += fillWing(data, targetId, "art");
                total += fillWing(data, targetId, "achievements");
                data.saveToDisk(level);
                sendActionResult(player, eco, true, "Filled all wings: " + total + " items added.");
                return true;
            }

            case "museum_clear_all": {
                UUID targetId;
                try {
                    targetId = UUID.fromString(jsonData.trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }
                MuseumData data = MuseumData.get(level);
                int total = 0;
                total += clearWing(data, targetId, "items");
                total += clearWing(data, targetId, "aquarium");
                total += clearWing(data, targetId, "wildlife");
                total += clearWing(data, targetId, "art");
                total += clearWing(data, targetId, "achievements");
                data.saveToDisk(level);
                sendActionResult(player, eco, true, "Cleared all wings: " + total + " items removed.");
                return true;
            }

            case "museum_reset_structure": {
                UUID targetId;
                try {
                    targetId = UUID.fromString(jsonData.trim());
                } catch (IllegalArgumentException e) {
                    sendActionResult(player, eco, false, "Invalid player UUID.");
                    return true;
                }
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                if (targetPlayer == null) {
                    sendActionResult(player, eco, false, "Player must be online to reset structure.");
                    return true;
                }
                MuseumDimensionManager dimMgr = MuseumDimensionManager.get(level);
                dimMgr.refreshMuseum(targetPlayer);
                sendActionResult(player, eco, true, "Museum structure rebuilt for " + targetPlayer.getGameProfile().name() + ".");
                return true;
            }

            default:
                return false;
        }
    }

    private static int fillWing(MuseumData data, UUID playerId, String wing) {
        int count = 0;
        switch (wing) {
            case "items": {
                Set<String> allItems = getAllCatalogItems();
                for (String itemId : allItems) {
                    if (data.donateItem(playerId, itemId)) {
                        count++;
                    }
                }
                break;
            }
            case "aquarium": {
                for (AquariumCatalog.MobEntry entry : AquariumCatalog.ENTRIES) {
                    if (data.donateMob(playerId, entry.entityId())) {
                        count++;
                    }
                }
                break;
            }
            case "wildlife": {
                for (WildlifeCatalog.MobEntry entry : WildlifeCatalog.ENTRIES) {
                    if (data.donateMob(playerId, entry.entityId())) {
                        count++;
                    }
                }
                break;
            }
            case "art": {
                for (ArtCatalog.ArtEntry entry : ArtCatalog.ENTRIES) {
                    if (data.donateArt(playerId, entry.id())) {
                        count++;
                    }
                }
                break;
            }
            case "achievements": {
                for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
                    data.recordAchievement(playerId, entry.advancementId());
                    count++;
                }
                break;
            }
        }
        return count;
    }

    private static int clearWing(MuseumData data, UUID playerId, String wing) {
        // To clear a wing we need to mutate the internal sets in MuseumData.
        // The getters return Collections.emptySet() for players with no donations,
        // which is immutable. We first ensure a mutable set exists by donating a
        // dummy value then immediately removing it, or we simply check emptiness.
        int count = 0;
        switch (wing) {
            case "items": {
                // Ensure a mutable set exists by touching computeIfAbsent via donate
                data.donateItem(playerId, "__clear_marker__");
                Set<String> donated = data.getDonatedItems(playerId);
                donated.remove("__clear_marker__");
                Set<String> allItems = getAllCatalogItems();
                for (String itemId : allItems) {
                    if (donated.remove(itemId)) {
                        count++;
                    }
                }
                break;
            }
            case "aquarium": {
                data.donateMob(playerId, "__clear_marker__");
                Set<String> donatedMobs = data.getDonatedMobs(playerId);
                donatedMobs.remove("__clear_marker__");
                for (AquariumCatalog.MobEntry entry : AquariumCatalog.ENTRIES) {
                    if (donatedMobs.remove(entry.entityId())) {
                        count++;
                    }
                }
                break;
            }
            case "wildlife": {
                data.donateMob(playerId, "__clear_marker__");
                Set<String> donatedMobs = data.getDonatedMobs(playerId);
                donatedMobs.remove("__clear_marker__");
                for (WildlifeCatalog.MobEntry entry : WildlifeCatalog.ENTRIES) {
                    if (donatedMobs.remove(entry.entityId())) {
                        count++;
                    }
                }
                break;
            }
            case "art": {
                data.donateArt(playerId, "__clear_marker__");
                Set<String> donatedArt = data.getDonatedArt(playerId);
                donatedArt.remove("__clear_marker__");
                for (ArtCatalog.ArtEntry entry : ArtCatalog.ENTRIES) {
                    if (donatedArt.remove(entry.id())) {
                        count++;
                    }
                }
                break;
            }
            case "achievements": {
                data.recordAchievement(playerId, "__clear_marker__");
                Set<String> donatedAch = data.getCompletedAchievements(playerId);
                donatedAch.remove("__clear_marker__");
                for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
                    if (donatedAch.remove(entry.advancementId())) {
                        count++;
                    }
                }
                break;
            }
        }
        return count;
    }

    private static Set<String> getAllCatalogItems() {
        Set<String> all = new HashSet<>();
        for (List<String> items : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            all.addAll(items);
        }
        return all;
    }

    private static String resolveName(UUID uuid, ServerLevel level) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return uuid.toString().substring(0, 8);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer((ServerPlayer) player, (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private static void sendActionResult(ServerPlayer player, EconomyManager eco, boolean success, String message) {
        String json = "{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}";
        sendResponse(player, "museum_action_result", json, eco);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
