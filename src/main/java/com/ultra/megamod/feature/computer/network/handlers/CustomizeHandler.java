package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.prestige.MasteryMarkManager;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Server-side handler for the Customize computer app.
 * Manages badge customization, prestige name colors, and mastery mark purchases.
 * TODO: Reconnect badge/branch features with Pufferfish Skills API
 */
public class CustomizeHandler {

    // Per-player prestige name color overrides
    private static final Map<UUID, String> prestigeColorOverrides = new HashMap<>();

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "customize_request" -> {
                sendData(player, level, eco);
                return true;
            }
            case "customize_badge_reset" -> {
                // TODO: Reconnect with Pufferfish Skills API (was SkillBadges.clearCustomBadge)
                sendResult(player, true, "Badge reset to auto", eco);
                return true;
            }
            case "customize_badge_title" -> {
                // TODO: Reconnect with Pufferfish Skills API (was SkillBadges + SkillBranch validation)
                sendResult(player, false, "Badge system being rebuilt", eco);
                return true;
            }
            case "customize_badge_color" -> {
                // TODO: Reconnect with Pufferfish Skills API (was SkillBadges.setCustomBadge)
                sendResult(player, false, "Badge system being rebuilt", eco);
                return true;
            }
            case "customize_prestige_color" -> {
                handlePrestigeColor(player, jsonData, level, eco);
                return true;
            }
            case "customize_buy" -> {
                handleBuy(player, jsonData, level, eco);
                return true;
            }
        }
        return false;
    }

    private static void sendData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        ServerLevel overworld = player.level().getServer().overworld();
        MasteryMarkManager marks = MasteryMarkManager.get(overworld);
        PrestigeManager prestige = PrestigeManager.get(overworld);

        JsonObject data = new JsonObject();
        data.addProperty("marks", marks.getMarks(uuid));
        int tp = prestige.getTotalPrestige(uuid);
        data.addProperty("total_prestige", tp);
        data.addProperty("is_admin", com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player));
        data.addProperty("has_prestiged", tp > 0);

        // Badge info stubbed — old SkillBadges system removed
        data.addProperty("has_custom", false);
        data.addProperty("badge_title", "");
        data.addProperty("badge_color", "");

        // Prestige color
        data.addProperty("prestige_color", prestigeColorOverrides.getOrDefault(uuid, "default"));

        // Boost levels
        data.addProperty("coin_boost", getBoostLevel(marks, uuid, "coin_boost"));
        data.addProperty("xp_boost", getBoostLevel(marks, uuid, "xp_boost"));

        // Branches stubbed — old SkillBranch enum removed
        data.add("branches", new JsonArray());

        ComputerDataPayload response = new ComputerDataPayload(
            "customize_data", data.toString(),
            eco.getWallet(uuid), eco.getBank(uuid)
        );
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) response, (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private static void handlePrestigeColor(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String color = obj.get("color").getAsString();

            UUID uuid = player.getUUID();
            ServerLevel overworld = player.level().getServer().overworld();
            PrestigeManager prestige = PrestigeManager.get(overworld);
            int totalPrestige = prestige.getTotalPrestige(uuid);

            if (totalPrestige < 5 && !"default".equals(color)) {
                sendResult(player, false, "Need prestige 5+ to change name color", eco);
                return;
            }

            prestigeColorOverrides.put(uuid, color);
            sendResult(player, true, "Name color set to " + color, eco);
        } catch (Exception e) {
            sendResult(player, false, "Error setting color", eco);
        }
    }

    private static void handleBuy(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            // Lock purchases behind prestige (admin bypass)
            ServerLevel overworld = player.level().getServer().overworld();
            PrestigeManager pres = PrestigeManager.get(overworld);
            boolean admin = com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player);
            if (pres.getTotalPrestige(player.getUUID()) < 1 && !admin) {
                sendResult(player, false, "Prestige a Skill Tree first to unlock Mastery Marks!", eco);
                return;
            }

            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String item = obj.get("item").getAsString();

            UUID uuid = player.getUUID();
            MasteryMarkManager marks = MasteryMarkManager.get(overworld);

            switch (item) {
                case "buy_coin_boost" -> {
                    if (!marks.spendMarks(uuid, 100)) {
                        sendResult(player, false, "Not enough Marks! Need 100", eco);
                        return;
                    }
                    marks.awardMilestone(player, "purchased_coin_boost_" + System.currentTimeMillis(), 0, "");
                    marks.saveToDisk(overworld);
                    sendResult(player, true, "Purchased +5% Coin Drops!", eco);
                }
                case "buy_xp_boost" -> {
                    if (!marks.spendMarks(uuid, 100)) {
                        sendResult(player, false, "Not enough Marks! Need 100", eco);
                        return;
                    }
                    marks.saveToDisk(overworld);
                    sendResult(player, true, "Purchased +5% XP Gain!", eco);
                }
                case "buy_furniture_crate" -> {
                    if (!marks.spendMarks(uuid, 30)) {
                        sendResult(player, false, "Not enough Marks! Need 30", eco);
                        return;
                    }
                    // Give random furniture item
                    var catalog = com.ultra.megamod.feature.economy.shop.FurnitureShop.getCatalog();
                    if (!catalog.isEmpty()) {
                        var rng = new java.util.Random();
                        var shopItem = catalog.get(rng.nextInt(catalog.size()));
                        var id = net.minecraft.resources.Identifier.parse(shopItem.itemId());
                        var opt = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id);
                        if (opt.isPresent()) {
                            var stack = new net.minecraft.world.item.ItemStack((net.minecraft.world.level.ItemLike) opt.get());
                            if (!player.getInventory().add(stack)) {
                                player.spawnAtLocation(overworld, stack);
                            }
                        }
                    }
                    marks.saveToDisk(overworld);
                    sendResult(player, true, "Furniture crate opened!", eco);
                }
                default -> sendResult(player, false, "Unknown item", eco);
            }
        } catch (Exception e) {
            sendResult(player, false, "Error purchasing", eco);
        }
    }

    private static int getBoostLevel(MasteryMarkManager marks, UUID uuid, String boostType) {
        // Count how many times this boost was purchased by checking claimed milestones
        int level = 0;
        for (int i = 0; i < 100; i++) {
            if (marks.hasClaimed(uuid, "purchased_" + boostType + "_" + i)) level++;
        }
        return level;
    }

    /** Get the prestige name color override for a player, or null for default behavior. */
    public static String getPrestigeColorOverride(UUID playerId) {
        return prestigeColorOverrides.getOrDefault(playerId, "default");
    }

    private static void sendResult(ServerPlayer player, boolean success, String msg, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("msg", msg);
        ComputerDataPayload response = new ComputerDataPayload(
            "customize_result", obj.toString(),
            eco.getWallet(player.getUUID()), eco.getBank(player.getUUID())
        );
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) response, (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
