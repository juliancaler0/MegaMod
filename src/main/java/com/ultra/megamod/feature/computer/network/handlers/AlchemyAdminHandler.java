package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.alchemy.AlchemyManager;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry.BrewingRecipe;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry.GrindingRecipe;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Network handler for the alchemy admin panel.
 * Queries AlchemyManager and AlchemyRecipeRegistry for real data.
 */
public class AlchemyAdminHandler {

    // Potion value by tier (for economy tab)
    private static final int[] TIER_VALUES = {0, 50, 100, 200, 400, 800};

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("alchemy_admin_")) return false;
        if (!AdminSystem.isAdmin(player)) return false;

        AlchemyManager mgr = AlchemyManager.get(level);

        switch (action) {
            case "alchemy_admin_request": {
                sendAlchemyData(player, level, eco, mgr);
                return true;
            }
            case "alchemy_admin_toggle": {
                if (jsonData != null && !jsonData.isEmpty()) {
                    mgr.toggleRecipe(jsonData.trim());
                    mgr.saveToDisk(level);
                }
                sendAlchemyData(player, level, eco, mgr);
                return true;
            }
            case "alchemy_admin_grant_all": {
                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        UUID targetUuid = UUID.fromString(jsonData.trim());
                        mgr.discoverAllRecipes(targetUuid);
                        mgr.saveToDisk(level);
                    } catch (IllegalArgumentException ignored) {}
                }
                sendAlchemyData(player, level, eco, mgr);
                return true;
            }
            default:
                return false;
        }
    }

    private static void sendAlchemyData(ServerPlayer player, ServerLevel level,
                                         EconomyManager eco, AlchemyManager mgr) {
        JsonObject root = new JsonObject();

        List<BrewingRecipe> allBrewing = AlchemyRecipeRegistry.getAllBrewingRecipes();
        List<GrindingRecipe> allGrinding = AlchemyRecipeRegistry.getAllGrindingRecipes();
        Map<UUID, ?> allPlayers = mgr.getAllPlayerData();
        PlayerList playerList = level.getServer().getPlayerList();

        // ==================== Aggregate Stats ====================
        int totalBrewed = 0;
        int totalDiscoveries = 0;
        int totalPotionValue = 0;

        for (UUID uuid : allPlayers.keySet()) {
            totalBrewed += mgr.getTotalBrews(uuid);
            totalDiscoveries += mgr.getDiscoveredRecipes(uuid).size();
        }

        // Potion value = sum of (brew count * tier value) across all players
        for (UUID uuid : allPlayers.keySet()) {
            Map<String, Integer> brewCounts = mgr.getAllBrewCounts(uuid);
            for (Map.Entry<String, Integer> entry : brewCounts.entrySet()) {
                BrewingRecipe recipe = AlchemyRecipeRegistry.getBrewingByOutput(entry.getKey());
                if (recipe != null) {
                    int tierVal = recipe.tier() < TIER_VALUES.length ? TIER_VALUES[recipe.tier()] : 100;
                    totalPotionValue += entry.getValue() * tierVal;
                }
            }
        }

        root.addProperty("totalBrewed", totalBrewed);
        root.addProperty("totalDiscoveries", totalDiscoveries);
        root.addProperty("totalRecipes", allBrewing.size() + allGrinding.size());
        root.addProperty("totalPotionValue", totalPotionValue);
        root.addProperty("potionsInCirculation", totalBrewed); // Each brew = 1 potion

        // ==================== Recipes ====================
        JsonArray recipesArr = new JsonArray();

        for (BrewingRecipe recipe : allBrewing) {
            JsonObject r = new JsonObject();
            r.addProperty("name", AlchemyRecipeRegistry.getPotionDisplayName(recipe.output()));
            r.addProperty("id", recipe.id());

            // Aggregate brew count across all players for this recipe's output
            int timesBrewed = 0;
            int discoveryCount = 0;
            for (UUID uuid : allPlayers.keySet()) {
                timesBrewed += mgr.getBrewCount(uuid, recipe.output());
                if (mgr.hasDiscovered(uuid, recipe.id())) discoveryCount++;
            }
            r.addProperty("timesBrewed", timesBrewed);
            r.addProperty("discoveryCount", discoveryCount);
            r.addProperty("enabled", !mgr.isRecipeDisabled(recipe.id()));
            r.addProperty("difficulty", getDifficulty(recipe.tier()));

            recipesArr.add(r);
        }

        for (GrindingRecipe recipe : allGrinding) {
            JsonObject r = new JsonObject();
            r.addProperty("name", AlchemyRecipeRegistry.getReagentDisplayName(recipe.output()));
            r.addProperty("id", recipe.id());
            r.addProperty("timesBrewed", 0); // Grinding doesn't brew
            int discoveryCount = 0;
            for (UUID uuid : allPlayers.keySet()) {
                if (mgr.hasDiscovered(uuid, recipe.id())) discoveryCount++;
            }
            r.addProperty("discoveryCount", discoveryCount);
            r.addProperty("enabled", !mgr.isRecipeDisabled(recipe.id()));
            r.addProperty("difficulty", "Easy");
            recipesArr.add(r);
        }

        root.add("recipes", recipesArr);

        // ==================== Players ====================
        JsonArray playersArr = new JsonArray();

        for (UUID uuid : allPlayers.keySet()) {
            JsonObject p = new JsonObject();

            // Try to resolve player name
            String name = resolvePlayerName(level, uuid);
            p.addProperty("name", name);
            p.addProperty("uuid", uuid.toString());
            p.addProperty("level", mgr.getAlchemyLevel(uuid));
            p.addProperty("xp", mgr.getTotalBrews(uuid));
            p.addProperty("discovered", mgr.getDiscoveredRecipes(uuid).size());
            p.addProperty("brewed", mgr.getTotalBrews(uuid));

            playersArr.add(p);
        }

        root.add("players", playersArr);

        // ==================== Ingredients ====================
        JsonArray ingredientsArr = new JsonArray();

        // Build reagent usage from brew counts + recipe definitions
        Map<String, Integer> reagentUsage = new LinkedHashMap<>();
        Map<String, String> reagentSources = new LinkedHashMap<>();

        // Map grinding outputs to their source inputs
        for (GrindingRecipe gr : allGrinding) {
            String reagentName = AlchemyRecipeRegistry.getReagentDisplayName(gr.output());
            reagentSources.put(gr.output(), formatInputNames(gr.inputs()));
            reagentUsage.putIfAbsent(gr.output(), 0);
        }

        // Count reagent usage from all brewing recipes brewed by all players
        for (UUID uuid : allPlayers.keySet()) {
            Map<String, Integer> brewCounts = mgr.getAllBrewCounts(uuid);
            for (Map.Entry<String, Integer> entry : brewCounts.entrySet()) {
                BrewingRecipe recipe = AlchemyRecipeRegistry.getBrewingByOutput(entry.getKey());
                if (recipe != null) {
                    for (String reagent : recipe.reagentList()) {
                        reagentUsage.merge(reagent, entry.getValue(), Integer::sum);
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> entry : reagentUsage.entrySet()) {
            JsonObject ig = new JsonObject();
            ig.addProperty("name", AlchemyRecipeRegistry.getReagentDisplayName(entry.getKey()));
            ig.addProperty("timesUsed", entry.getValue());
            ig.addProperty("available", 0); // Can't track inventory across players
            ig.addProperty("source", reagentSources.getOrDefault(entry.getKey(), "Unknown"));
            ingredientsArr.add(ig);
        }

        root.add("ingredients", ingredientsArr);

        sendResponse(player, "alchemy_admin_data", root.toString(), eco);
    }

    private static String getDifficulty(int tier) {
        return switch (tier) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Medium";
            case 4 -> "Hard";
            case 5 -> "Hard";
            default -> "Easy";
        };
    }

    private static String resolvePlayerName(ServerLevel level, UUID uuid) {
        // Try online player first
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return uuid.toString().substring(0, 8);
    }

    private static String formatInputNames(List<String> inputs) {
        StringBuilder sb = new StringBuilder();
        for (String input : inputs) {
            if (!sb.isEmpty()) sb.append(", ");
            // "minecraft:blaze_powder" -> "Blaze Powder"
            String name = input.contains(":") ? input.substring(input.indexOf(':') + 1) : input;
            name = name.replace("_", " ");
            StringBuilder cap = new StringBuilder();
            for (String word : name.split(" ")) {
                if (!cap.isEmpty()) cap.append(" ");
                cap.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
            sb.append(cap);
        }
        return sb.toString();
    }

    private static void sendResponse(ServerPlayer player, String dataType, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                (CustomPacketPayload) new ComputerDataPayload(dataType, json, wallet, bank));
    }
}
