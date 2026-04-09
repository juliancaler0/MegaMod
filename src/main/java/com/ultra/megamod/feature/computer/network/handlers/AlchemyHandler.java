package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.alchemy.AlchemyManager;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Computer network handler for alchemy wiki/discovery in the computer app.
 */
public class AlchemyHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "alchemy_request" -> {
                UUID uuid = player.getUUID();
                AlchemyManager mgr = AlchemyManager.get(level);

                Set<String> discovered = mgr.getDiscoveredRecipes(uuid);
                Map<String, Integer> brewCounts = mgr.getAllBrewCounts(uuid);
                int alchLevel = mgr.getAlchemyLevel(uuid);
                int totalBrews = mgr.getTotalBrews(uuid);

                StringBuilder sb = new StringBuilder("{");
                sb.append("\"alchemyLevel\":").append(alchLevel).append(",");
                sb.append("\"totalBrews\":").append(totalBrews).append(",");

                // Discovered recipes
                sb.append("\"discovered\":[");
                boolean first = true;
                for (String recipeId : discovered) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(escapeJson(recipeId)).append("\"");
                    first = false;
                }
                sb.append("],");

                // Brew counts
                sb.append("\"brewCounts\":{");
                first = true;
                for (Map.Entry<String, Integer> entry : brewCounts.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
                    first = false;
                }
                sb.append("}}");

                sendResponse(player, "alchemy_data", sb.toString(), eco);
                return true;
            }

            case "alchemy_recipe_list" -> {
                UUID uuid = player.getUUID();
                AlchemyManager mgr = AlchemyManager.get(level);
                Set<String> discovered = mgr.getDiscoveredRecipes(uuid);

                List<AlchemyRecipeRegistry.BrewingRecipe> allRecipes = AlchemyRecipeRegistry.getAllBrewingRecipes();

                StringBuilder sb = new StringBuilder("{\"recipes\":[");
                boolean first = true;
                for (AlchemyRecipeRegistry.BrewingRecipe recipe : allRecipes) {
                    if (!first) sb.append(",");
                    sb.append("{");
                    sb.append("\"id\":\"").append(escapeJson(recipe.id())).append("\",");
                    sb.append("\"tier\":").append(recipe.tier()).append(",");
                    sb.append("\"tierReq\":\"").append(escapeJson(AlchemyRecipeRegistry.getTierRequirement(recipe.tier()))).append("\",");

                    boolean isDiscovered = discovered.contains(recipe.id());
                    sb.append("\"discovered\":").append(isDiscovered).append(",");

                    if (isDiscovered) {
                        sb.append("\"output\":\"").append(escapeJson(recipe.output())).append("\",");
                        sb.append("\"outputName\":\"").append(escapeJson(AlchemyRecipeRegistry.getPotionDisplayName(recipe.output()))).append("\",");
                        sb.append("\"reagents\":[");
                        boolean firstR = true;
                        for (String r : recipe.reagentList()) {
                            if (!firstR) sb.append(",");
                            sb.append("\"").append(escapeJson(AlchemyRecipeRegistry.getReagentDisplayName(r))).append("\"");
                            firstR = false;
                        }
                        sb.append("]");
                    } else {
                        sb.append("\"output\":\"???\",");
                        sb.append("\"outputName\":\"???\",");
                        sb.append("\"reagents\":[\"???\",\"???\",\"???\"]");
                    }

                    sb.append("}");
                    first = false;
                }
                sb.append("]}");

                sendResponse(player, "alchemy_recipe_list_data", sb.toString(), eco);
                return true;
            }
        }
        return false;
    }

    private static void sendResponse(ServerPlayer player, String responseType, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                new ComputerDataPayload(responseType, json, wallet, bank));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
