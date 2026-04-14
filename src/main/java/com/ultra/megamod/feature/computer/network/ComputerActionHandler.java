/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.computer.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.admin.AdminWarpManager;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.shop.MegaShop;
import com.ultra.megamod.feature.multiplayer.PlayerStatistics;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.RelicItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import com.ultra.megamod.feature.skills.SkillAttributeApplier;
import com.ultra.megamod.feature.skills.SkillEvents;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComputerActionHandler {
    public static void handleAction(ServerPlayer player, String action, String jsonData) {
        try {
            ServerLevel level = player.level();
            EconomyManager eco = EconomyManager.get(level);
            try {
                handleActionInner(player, action, jsonData, level, eco);
            } catch (Exception e) {
                MegaMod.LOGGER.error("Error handling computer action '{}' for player {}: {}", action, player.getGameProfile().name(), e.getMessage(), e);
                sendResponse(player, "error", "{\"success\":false,\"message\":\"Server error processing action.\"}", eco);
            }
        } catch (Exception e) {
            // level() or EconomyManager.get() failed — send raw error without eco
            MegaMod.LOGGER.error("Critical error in computer action '{}' for player {}: {}", action, player.getGameProfile().name(), e.getMessage(), e);
            PacketDistributor.sendToPlayer(player, new ComputerDataPayload("error", "{\"success\":false,\"message\":\"Critical server error.\"}", 0, 0));
        }
    }

    private static void handleActionInner(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        // Block all admin-only actions for non-admin players (packet spoofing protection)
        if (!AdminSystem.isAdmin(player)) {
            if (action.startsWith("admin_") || action.startsWith("eco_") || action.startsWith("cosm_") || action.startsWith("we_")
                    || action.startsWith("shop_admin_") || action.startsWith("research_")
                    || action.startsWith("vanish_") || action.startsWith("alias_")
                    || action.startsWith("deathlog_") || action.startsWith("loot_")
                    || action.startsWith("undo_") || action.startsWith("cleanup_")
                    || action.startsWith("warp_") || action.startsWith("spells_")
                    || action.equals("execute_command")
                    || action.equals("skill_add_xp") || action.equals("skill_add_points")
                    || action.equals("skill_set_level") || action.equals("skill_reset_tree")
                    || action.equals("skill_max_all_trees") || action.equals("skill_set_admin_xp_mult") || action.equals("skill_set_admin_only_xp_boost")
                    || action.equals("dungeon_force_extract")
                    || action.equals("request_player_detail")
                    || action.equals("request_economy") || action.equals("request_skills")
                    || action.equals("request_cosmetics")
                    || action.equals("request_dungeons") || action.equals("request_warps")
                    || action.equals("request_audit_log") || action.equals("request_player_audit")
                    || action.equals("request_party_view") || action.equals("request_bounty_view")
                    || action.equals("request_game_scores")
                    || action.equals("request_performance") || action.equals("request_entities")
                    || action.equals("request_server_logs") || action.equals("request_suggestions")
                    || action.equals("admin_class_change") || action.equals("admin_class_list")) {
                return;
            }
        }
        // Delegate to new panel handlers first
        if (com.ultra.megamod.feature.computer.network.handlers.MuseumManagerHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.InventoryViewerHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.DungeonLeaderboardHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.FeatureTogglesHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.ModerationHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.StructureLocatorHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.SchedulerHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MusicPlayerHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.EncyclopediaHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.NotesHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MapHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.LeaderboardHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MailHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.FriendsHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.BotControlHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.CustomizeHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MobShowcaseHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.FurnitureShowcaseHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.AdminModulesHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.PartyHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.WarpHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.TradeHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MarketplaceHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.marketplace.network.TradingTerminalHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.QuestsHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.ChallengesHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.ArenaHandler.handle(player, action, jsonData, level, eco)) return;
        // Citizen/Colony system handlers
        if (com.ultra.megamod.feature.citizen.network.handlers.TownHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.citizen.network.handlers.AdminCitizensHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.citizen.network.handlers.AdminCitizenHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.citizen.network.handlers.PostBoxHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.casino.network.CasinoHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.casino.network.CasinoManagerHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.CorruptionAdminHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.CorruptionHandler.handle(player, action, jsonData, level, eco)) return;
        // AlchemyHandler / AlchemyAdminHandler deleted — Reliquary apothecary replaces them.
        if (com.ultra.megamod.feature.computer.network.handlers.ReliquaryAdminHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.EconomyAnalyticsHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.MarketplaceAdminHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.SystemHealthHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.PrestigeShopHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.AdminSearchHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.WorldEditHandler.handle(player, action, jsonData, level, eco)) return;
        if (com.ultra.megamod.feature.computer.network.handlers.SpellsAdminHandler.handle(player, action, jsonData, level, eco)) return;
        switch (action) {
            case "execute_command": {
                String result = AdminSystem.executeCommand(player, jsonData);
                String escaped = result.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
                ComputerActionHandler.sendResponse(player, "command_result", "{\"success\":true,\"output\":\"" + escaped + "\"}", eco);
                break;
            }
            case "transfer_to_bank": {
                int amount = Integer.parseInt(jsonData);
                if (amount <= 0) {
                    ComputerActionHandler.sendResponse(player, "transfer_result", "{\"success\":false}", eco);
                    break;
                }
                boolean ok = eco.transferToBank(player.getUUID(), amount);
                ComputerActionHandler.sendResponse(player, "transfer_result", "{\"success\":" + ok + "}", eco);
                break;
            }
            case "transfer_to_wallet": {
                int amount = Integer.parseInt(jsonData);
                if (amount <= 0) {
                    ComputerActionHandler.sendResponse(player, "transfer_result", "{\"success\":false}", eco);
                    break;
                }
                boolean ok = eco.transferToWallet(player.getUUID(), amount);
                ComputerActionHandler.sendResponse(player, "transfer_result", "{\"success\":" + ok + "}", eco);
                break;
            }
            case "request_shop": {
                MegaShop shop = MegaShop.get(level);
                String items = shop.getTodaysItemsJson();
                ComputerActionHandler.sendResponse(player, "shop_data", items, eco);
                break;
            }
            case "buy_shop_item": {
                MegaShop shop = MegaShop.get(level);
                boolean ok = shop.buyItem(player, Integer.parseInt(jsonData));
                ComputerActionHandler.sendResponse(player, "buy_result", "{\"success\":" + ok + "}", eco);
                break;
            }
            case "sell_shop_item": {
                MegaShop shop = MegaShop.get(level);
                // Client sends "index:quantity" format
                int sellIndex;
                int sellQty = 1;
                if (jsonData.contains(":")) {
                    String[] sellParts = jsonData.split(":");
                    sellIndex = Integer.parseInt(sellParts[0]);
                    sellQty = Math.max(1, Integer.parseInt(sellParts[1]));
                } else {
                    sellIndex = Integer.parseInt(jsonData);
                }
                boolean sellOk = false;
                for (int sq = 0; sq < sellQty; sq++) {
                    if (shop.sellItem(player, sellIndex)) {
                        sellOk = true;
                    } else {
                        break;
                    }
                }
                ComputerActionHandler.sendResponse(player, "sell_result", "{\"success\":" + sellOk + "}", eco);
                break;
            }
            case "request_furniture": {
                String furnitureJson = com.ultra.megamod.feature.economy.shop.FurnitureShop.getCatalogJson();
                ComputerActionHandler.sendResponse(player, "furniture_data", furnitureJson, eco);
                break;
            }
            case "buy_furniture_item": {
                boolean ok = com.ultra.megamod.feature.economy.shop.FurnitureShop.buyItem(player, Integer.parseInt(jsonData));
                ComputerActionHandler.sendResponse(player, "buy_result", "{\"success\":" + ok + "}", eco);
                break;
            }
            case "request_stats": {
                PlayerStatistics stats = PlayerStatistics.get(player.level());
                UUID pid = player.getUUID();
                String json = "{\"kills\":" + stats.getStat(pid, "kills") + ",\"deaths\":" + stats.getStat(pid, "deaths") + ",\"mobKills\":" + stats.getStat(pid, "mobKills") + ",\"blocksBroken\":" + stats.getStat(pid, "blocksBroken") + ",\"blocksPlaced\":" + stats.getStat(pid, "blocksPlaced") + ",\"playTimeTicks\":" + stats.getStat(pid, "playTimeTicks") + ",\"damageDealt\":" + stats.getStat(pid, "damageDealt") + ",\"damageTaken\":" + stats.getStat(pid, "damageTaken") + "}";
                ComputerActionHandler.sendResponse(player, "stats_data", json, eco);
                break;
            }
            case "send_message": {
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 2) return;
                ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target != null) {
                    target.sendSystemMessage((Component)Component.literal((String)("[PM from " + player.getName().getString() + "] " + parts[1])).withStyle(ChatFormatting.LIGHT_PURPLE));
                    player.sendSystemMessage((Component)Component.literal((String)("[PM to " + parts[0] + "] " + parts[1])).withStyle(ChatFormatting.GRAY));
                }
                ComputerActionHandler.sendResponse(player, "message_result", "{\"success\":" + (target != null) + "}", eco);
                break;
            }
            case "request_economy": {
                ComputerActionHandler.sendEconomyData(player, eco, level);
                break;
            }
            case "eco_modify": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                String targetType = parts[2];
                int oldVal = "wallet".equals(targetType) ? eco.getWallet(targetId) : eco.getBank(targetId);
                if ("wallet".equals(targetType)) {
                    eco.setWallet(targetId, Math.max(0, oldVal + amount));
                } else {
                    eco.setBank(targetId, Math.max(0, oldVal + amount));
                }
                eco.saveToDisk(level);
                String modName = ComputerActionHandler.resolveName(targetId, level);
                eco.addAuditEntry(modName, amount > 0 ? "ADMIN_ADD" : "ADMIN_REMOVE", amount, targetType + " modified by admin");
                // Record undo
                com.ultra.megamod.feature.computer.admin.AdminUndoManager.record(
                    com.ultra.megamod.feature.computer.admin.AdminUndoManager.UndoType.ECO_MODIFY,
                    player.getGameProfile().name(), modName, targetId,
                    targetType + " " + (amount > 0 ? "+" : "") + amount + " on " + modName,
                    "eco_set:" + targetId + ":" + oldVal + ":" + targetType);
                ComputerActionHandler.sendEconomyData(player, eco, level);
                break;
            }
            case "eco_set": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                int amount = Math.max(0, Integer.parseInt(parts[1]));
                String targetType = parts[2];
                int oldVal = "wallet".equals(targetType) ? eco.getWallet(targetId) : eco.getBank(targetId);
                if ("wallet".equals(targetType)) {
                    eco.setWallet(targetId, amount);
                } else {
                    eco.setBank(targetId, amount);
                }
                eco.saveToDisk(level);
                String modName = ComputerActionHandler.resolveName(targetId, level);
                eco.addAuditEntry(modName, "ADMIN_SET", amount, targetType + " set to " + amount + " by admin");
                // Record undo
                com.ultra.megamod.feature.computer.admin.AdminUndoManager.record(
                    com.ultra.megamod.feature.computer.admin.AdminUndoManager.UndoType.ECO_SET,
                    player.getGameProfile().name(), modName, targetId,
                    targetType + " set " + oldVal + " -> " + amount + " on " + modName,
                    "eco_set:" + targetId + ":" + oldVal + ":" + targetType);
                ComputerActionHandler.sendEconomyData(player, eco, level);
                break;
            }
            case "eco_bulk": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int amount = Integer.parseInt(parts[0]);
                String targetType = parts[1];
                Map<UUID, int[]> allPlayers = eco.getAllPlayerData();
                for (Map.Entry<UUID, int[]> entry : allPlayers.entrySet()) {
                    UUID pid = entry.getKey();
                    if ("wallet".equals(targetType)) {
                        eco.setWallet(pid, Math.max(0, eco.getWallet(pid) + amount));
                    } else {
                        eco.setBank(pid, Math.max(0, eco.getBank(pid) + amount));
                    }
                }
                eco.saveToDisk(level);
                eco.addAuditEntry(player.getGameProfile().name(), amount > 0 ? "ADMIN_BULK_ADD" : "ADMIN_BULK_SUB", amount, targetType + " bulk modified by admin (" + allPlayers.size() + " players)");
                ComputerActionHandler.sendEconomyData(player, eco, level);
                break;
            }
            case "request_skills": {
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "minigame_submit_score": {
                // Format: game:score (e.g. "snake:42")
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                String game = parts[0];
                int score = Integer.parseInt(parts[1]);
                var mgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
                boolean isNew = mgr.submitScore(player.getUUID(), game, score);
                mgr.saveToDisk(level);
                String msg = isNew ? "New high score!" : "Score submitted";
                ComputerActionHandler.sendResponse(player, "minigame_score_result",
                    "{\"game\":\"" + game + "\",\"score\":" + score + ",\"newHighScore\":" + isNew
                    + ",\"highScore\":" + mgr.getHighScore(player.getUUID(), game) + "}", eco);
                break;
            }
            case "minigame_get_scores": {
                var mgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
                UUID pid = player.getUUID();
                String resp = "{\"snake\":" + mgr.getHighScore(pid, "snake")
                    + ",\"tetris\":" + mgr.getHighScore(pid, "tetris")
                    + ",\"minesweeper\":" + mgr.getHighScore(pid, "minesweeper") + "}";
                ComputerActionHandler.sendResponse(player, "minigame_scores_data", resp, eco);
                break;
            }
            case "request_game_scores": {
                ComputerActionHandler.sendGameScoresData(player, eco, level);
                break;
            }
            case "admin_set_game_score": {
                // Format: uuid:game:score
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                String game = parts[1];
                int score = Integer.parseInt(parts[2]);
                var mgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
                mgr.setScore(targetId, game, score);
                mgr.saveToDisk(level);
                ComputerActionHandler.sendGameScoresData(player, eco, level);
                break;
            }
            case "admin_reset_game_scores": {
                UUID targetId = UUID.fromString(jsonData);
                var mgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
                mgr.setScore(targetId, "snake", 0);
                mgr.setScore(targetId, "tetris", 0);
                mgr.setScore(targetId, "minesweeper", 0);
                mgr.saveToDisk(level);
                ComputerActionHandler.sendGameScoresData(player, eco, level);
                break;
            }
            case "admin_class_change": {
                // Format: "targetUUID:CLASS_NAME" (e.g. "uuid:WIZARD")
                String[] classParts = jsonData.split(":");
                if (classParts.length < 2) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Invalid format. Use targetUUID:CLASS_NAME\"}", eco);
                    break;
                }
                try {
                    UUID classTargetId = UUID.fromString(classParts[0]);
                    String className = classParts[1].toUpperCase().trim();
                    com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass newClass =
                            com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass.valueOf(className);
                    com.ultra.megamod.feature.combat.PlayerClassManager pcm = com.ultra.megamod.feature.combat.PlayerClassManager.get(level);
                    com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass oldClass = pcm.getPlayerClass(classTargetId);
                    pcm.setClass(classTargetId, newClass);
                    pcm.saveToDisk(level);

                    // Notify target player if online
                    ServerPlayer classTarget = level.getServer().getPlayerList().getPlayer(classTargetId);
                    if (classTarget != null) {
                        classTarget.sendSystemMessage(
                                Component.literal("Your class has been changed to " + newClass.getDisplayName() + " by an admin.")
                                        .withStyle(ChatFormatting.GOLD));
                    }

                    String targetName = classTarget != null ? classTarget.getGameProfile().name() : classTargetId.toString();
                    ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"msg\":\"Changed " + targetName + " class from " + oldClass.getDisplayName() + " to " + newClass.getDisplayName() + "\"}", eco);
                } catch (IllegalArgumentException e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"msg\":\"Invalid class name. Valid: NONE, PALADIN, WARRIOR, WIZARD, ROGUE, RANGER\"}", eco);
                }
                break;
            }
            case "admin_class_list": {
                // Returns all online players with their current class
                com.ultra.megamod.feature.combat.PlayerClassManager pcm = com.ultra.megamod.feature.combat.PlayerClassManager.get(level);
                StringBuilder clsb = new StringBuilder("[");
                boolean firstPlayer = true;
                for (ServerPlayer onlinePlayer : level.getServer().getPlayerList().getPlayers()) {
                    if (!firstPlayer) clsb.append(",");
                    firstPlayer = false;
                    UUID pid = onlinePlayer.getUUID();
                    com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass cls = pcm.getPlayerClass(pid);
                    clsb.append("{\"uuid\":\"").append(pid)
                         .append("\",\"name\":\"").append(onlinePlayer.getGameProfile().name())
                         .append("\",\"class\":\"").append(cls.name())
                         .append("\",\"displayClass\":\"").append(cls.getDisplayName())
                         .append("\"}");
                }
                // Also include offline players from the full map
                java.util.Map<UUID, com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass> allClasses = pcm.getAllClasses();
                java.util.Set<UUID> onlineUuids = new java.util.HashSet<>();
                for (ServerPlayer op : level.getServer().getPlayerList().getPlayers()) {
                    onlineUuids.add(op.getUUID());
                }
                for (java.util.Map.Entry<UUID, com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass> entry : allClasses.entrySet()) {
                    if (onlineUuids.contains(entry.getKey())) continue;
                    if (!firstPlayer) clsb.append(",");
                    firstPlayer = false;
                    clsb.append("{\"uuid\":\"").append(entry.getKey())
                         .append("\",\"name\":\"(offline)")
                         .append("\",\"class\":\"").append(entry.getValue().name())
                         .append("\",\"displayClass\":\"").append(entry.getValue().getDisplayName())
                         .append("\"}");
                }
                clsb.append("]");
                ComputerActionHandler.sendResponse(player, "admin_class_data", clsb.toString(), eco);
                break;
            }
            case "admin_discover_all": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.encyclopedia.DiscoveryManager dm = com.ultra.megamod.feature.encyclopedia.DiscoveryManager.get(level);
                dm.discoverAll(targetId);
                dm.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Unlocked all discoveries\"}", eco);
                break;
            }
            case "admin_clear_discoveries": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.encyclopedia.DiscoveryManager dm = com.ultra.megamod.feature.encyclopedia.DiscoveryManager.get(level);
                dm.clearAll(targetId);
                dm.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Cleared all discoveries\"}", eco);
                break;
            }
            // admin_clear_cooldowns removed in Phase F — the legacy AbilityCooldownManager
            // is being retired in favor of SpellEngine's SpellCooldownManager. Use the
            // new Spells tab -> Cooldown Override panel instead.
            case "admin_view_parties": {
                StringBuilder sb = new StringBuilder("{\"parties\":[");
                var allParties = com.ultra.megamod.feature.computer.network.handlers.PartyHandler.getAllParties();
                boolean firstParty = true;
                for (var entry : allParties.entrySet()) {
                    if (!firstParty) sb.append(",");
                    firstParty = false;
                    var party = entry.getValue();
                    sb.append("{\"leader\":\"").append(ComputerActionHandler.resolveName(party.leader(), level)).append("\"");
                    sb.append(",\"members\":[");
                    boolean firstMember = true;
                    for (UUID memberId : party.members()) {
                        if (!firstMember) sb.append(",");
                        firstMember = false;
                        sb.append("\"").append(ComputerActionHandler.resolveName(memberId, level)).append("\"");
                    }
                    sb.append("]}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "parties_data", sb.toString(), eco);
                break;
            }
            case "admin_disband_all_parties": {
                com.ultra.megamod.feature.computer.network.handlers.PartyHandler.reset();
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"All parties disbanded\"}", eco);
                break;
            }
            case "admin_heal_self": {
                player.setHealth(player.getMaxHealth());
                player.removeAllEffects();
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 100, 2, false, false));
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Healed to full HP\"}", eco);
                break;
            }
            case "admin_feed_self": {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0f);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Hunger fully restored\"}", eco);
                break;
            }
            case "admin_cancel_bounty": {
                try {
                    int bountyId = Integer.parseInt(jsonData);
                    com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.cancelBountyAdmin(player, bountyId, level, eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Invalid bounty ID\"}", eco);
                }
                break;
            }
            case "admin_send_system_mail": {
                // Format: targetName:subject:body - send as chat message since MailHandler doesn't expose sendSystemMail
                String[] parts = jsonData.split(":", 3);
                if (parts.length >= 3) {
                    ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                    if (target != null) {
                        target.sendSystemMessage((Component)Component.literal((String)("\u00a76[\u00a7eSYSTEM MAIL\u00a76] \u00a7f" + parts[1] + ": " + parts[2])).withStyle(ChatFormatting.GOLD));
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"System message sent to " + parts[0] + "\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Player not online\"}", eco);
                    }
                }
                break;
            }
            case "shop_admin_refresh": {
                MegaShop shop = MegaShop.get(level);
                shop.forceRefresh();
                shop.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Shop rotation refreshed\"}", eco);
                break;
            }
            case "shop_admin_set_interval": {
                int hours = Integer.parseInt(jsonData);
                MegaShop shop = MegaShop.get(level);
                shop.setRefreshInterval(hours * 1200); // hours to ticks (1 MC hour = 1200 ticks)
                shop.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Shop interval set to " + hours + " MC hours\"}", eco);
                break;
            }
            case "shop_admin_set_price_mult": {
                double mult = Double.parseDouble(jsonData);
                MegaShop shop = MegaShop.get(level);
                shop.setGlobalPriceMultiplier(mult);
                shop.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Price multiplier set to " + String.format("%.1fx", mult) + "\"}", eco);
                break;
            }
            case "shop_admin_set_sell_pct": {
                double pct = Double.parseDouble(jsonData) / 100.0;
                MegaShop shop = MegaShop.get(level);
                shop.setSellPercentage(pct);
                shop.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Sell percentage set to " + (int)(pct*100) + "%\"}", eco);
                break;
            }
            case "shop_admin_get_config": {
                MegaShop shop = MegaShop.get(level);
                String config = "{\"intervalTicks\":" + shop.getRefreshIntervalTicks()
                    + ",\"priceMult\":" + shop.getGlobalPriceMultiplier()
                    + ",\"sellPct\":" + shop.getSellPercentage() + "}";
                ComputerActionHandler.sendResponse(player, "shop_config_data", config, eco);
                break;
            }
            case "skill_add_xp": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                int amount = Integer.parseInt(parts[2]);
                SkillManager skills = SkillManager.get(level);
                skills.addXp(targetId, tree, amount);
                skills.saveToDisk(level);
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                if (targetPlayer != null) {
                    SkillAttributeApplier.recalculate(targetPlayer);
                    SkillEvents.syncToClient(targetPlayer);
                }
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_add_points": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                int amount = Integer.parseInt(parts[2]);
                SkillManager skills = SkillManager.get(level);
                skills.addPoints(targetId, tree, amount);
                skills.saveToDisk(level);
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                if (targetPlayer != null) {
                    SkillAttributeApplier.recalculate(targetPlayer);
                    SkillEvents.syncToClient(targetPlayer);
                }
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_set_level": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                int lvl = Integer.parseInt(parts[2]);
                SkillManager skills = SkillManager.get(level);
                skills.setLevel(targetId, tree, lvl);
                skills.saveToDisk(level);
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                if (targetPlayer != null) {
                    SkillAttributeApplier.recalculate(targetPlayer);
                    SkillEvents.syncToClient(targetPlayer);
                }
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_reset_tree": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                SkillManager skills = SkillManager.get(level);
                skills.resetTree(targetId, tree);
                skills.saveToDisk(level);
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                if (targetPlayer != null) {
                    SkillAttributeApplier.recalculate(targetPlayer);
                    SkillEvents.syncToClient(targetPlayer);
                }
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_max_all_trees": {
                SkillManager skills = SkillManager.get(level);
                skills.maxOutAllTrees(player.getUUID());
                skills.saveToDisk(level);
                SkillAttributeApplier.recalculate(player);
                SkillEvents.syncToClient(player);
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_set_admin_xp_mult": {
                double mult = Double.parseDouble(jsonData);
                SkillManager skills = SkillManager.get(level);
                skills.setAdminXpMultiplier(mult);
                skills.saveToDisk(level);
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "skill_set_admin_only_xp_boost": {
                double boost = Double.parseDouble(jsonData);
                SkillManager skills2 = SkillManager.get(level);
                skills2.setAdminOnlyXpBoost(boost);
                skills2.saveToDisk(level);
                ComputerActionHandler.sendSkillData(player, eco, level);
                break;
            }
            case "request_cosmetics": {
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_toggle_badge": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.toggleSetting(targetId, "skill_badge");
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_toggle_particles": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.toggleSetting(targetId, "skill_particles");
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_prestige_up": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                boolean applied = false;
                for (SkillTreeType t : SkillTreeType.values()) {
                    if (prestige.getPrestigeLevel(targetId, t) < 5) {
                        prestige.prestige(targetId, t);
                        applied = true;
                        break;
                    }
                }
                if (applied) prestige.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_prestige_down": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                boolean applied = false;
                SkillTreeType[] trees = SkillTreeType.values();
                for (int i = trees.length - 1; i >= 0; i--) {
                    if (prestige.getPrestigeLevel(targetId, trees[i]) > 0) {
                        prestige.decrementPrestige(targetId, trees[i]);
                        applied = true;
                        break;
                    }
                }
                if (applied) prestige.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_tree_prestige_up": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                prestige.prestige(targetId, tree);
                prestige.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_tree_prestige_down": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                UUID targetId = UUID.fromString(parts[0]);
                SkillTreeType tree = SkillTreeType.valueOf(parts[1]);
                com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                prestige.decrementPrestige(targetId, tree);
                prestige.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_reset_respec": {
                UUID targetId = UUID.fromString(jsonData);
                SkillManager skills = SkillManager.get(level);
                skills.resetAllRespecCounts(targetId);
                skills.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "request_party_view": {
                ComputerActionHandler.sendPartyViewData(player, eco, level);
                break;
            }
            case "request_bounty_view": {
                ComputerActionHandler.sendBountyViewData(player, eco, level);
                break;
            }
            case "cosm_disband_party": {
                UUID leaderUuid = UUID.fromString(jsonData);
                // Force disband via party handler — kick all members
                ServerPlayer leaderPlayer = level.getServer().getPlayerList().getPlayer(leaderUuid);
                if (leaderPlayer != null) {
                    com.ultra.megamod.feature.computer.network.handlers.PartyHandler.handle(leaderPlayer, "party_disband", "", level, eco);
                }
                ComputerActionHandler.sendPartyViewData(player, eco, level);
                break;
            }
            case "cosm_remove_bounty": {
                int bountyId = Integer.parseInt(jsonData);
                com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.adminRemoveBounty(bountyId, level, eco);
                ComputerActionHandler.sendBountyViewData(player, eco, level);
                break;
            }
            case "cosm_set_badge": {
                // Format: uuid|title:color
                String[] mainParts = jsonData.split("\\|", 2);
                UUID targetId = UUID.fromString(mainParts[0]);
                String badgeInput = mainParts.length > 1 ? mainParts[1] : "";
                String title;
                String color = "white";
                if (badgeInput.contains(":")) {
                    String[] badgeParts = badgeInput.split(":", 2);
                    title = badgeParts[0].trim();
                    color = badgeParts[1].trim().toLowerCase();
                } else {
                    title = badgeInput.trim();
                }
                if (!title.isEmpty()) {
                    com.ultra.megamod.feature.skills.SkillBadges.setCustomBadge(targetId, title, color);
                    com.ultra.megamod.feature.skills.SkillBadges.saveToDisk(level);
                }
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "cosm_clear_badge": {
                UUID targetId = UUID.fromString(jsonData);
                com.ultra.megamod.feature.skills.SkillBadges.clearCustomBadge(targetId);
                com.ultra.megamod.feature.skills.SkillBadges.saveToDisk(level);
                ComputerActionHandler.sendCosmeticData(player, eco, level);
                break;
            }
            case "request_player_detail": {
                UUID targetId = UUID.fromString(jsonData);
                StringBuilder sb = new StringBuilder("{");
                String name = ComputerActionHandler.resolveName(targetId, level);
                ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                sb.append("\"name\":\"").append(name.replace("\"", "\\\"")).append("\"");
                sb.append(",\"uuid\":\"").append(targetId).append("\"");
                sb.append(",\"online\":").append(target != null);
                if (target != null) {
                    sb.append(",\"x\":").append((int) target.getX());
                    sb.append(",\"y\":").append((int) target.getY());
                    sb.append(",\"z\":").append((int) target.getZ());
                    sb.append(",\"dim\":\"").append(target.level().dimension().identifier().toString()).append("\"");
                    sb.append(",\"health\":").append((int) target.getHealth());
                    sb.append(",\"maxHealth\":").append((int) target.getMaxHealth());
                    sb.append(",\"food\":").append(target.getFoodData().getFoodLevel());
                    sb.append(",\"gamemode\":\"").append(target.gameMode.getGameModeForPlayer().getName()).append("\"");
                }
                sb.append(",\"wallet\":").append(eco.getWallet(targetId));
                sb.append(",\"bank\":").append(eco.getBank(targetId));
                SkillManager skills = SkillManager.get(level);
                sb.append(",\"skills\":{");
                boolean first = true;
                for (SkillTreeType tree : SkillTreeType.values()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("\"").append(tree.name()).append("\":[")
                      .append(skills.getLevel(targetId, tree)).append(",")
                      .append(skills.getXp(targetId, tree)).append("]");
                }
                sb.append("}");
                sb.append(",\"skillPoints\":").append(skills.getAvailablePoints(targetId));
                try {
                    sb.append(",\"relics\":{");
                    if (target != null) {
                        Map<AccessorySlotType, ItemStack> equipped = LibAccessoryLookup.getAllEquipped(target);
                        first = true;
                        for (Map.Entry<AccessorySlotType, ItemStack> e : equipped.entrySet()) {
                            if (e.getValue().isEmpty()) continue;
                            if (!first) sb.append(",");
                            first = false;
                            sb.append("\"").append(e.getKey().name()).append("\":\"")
                              .append(BuiltInRegistries.ITEM.getKey(e.getValue().getItem()).toString()).append("\"");
                        }
                    }
                    sb.append("}");
                } catch (Exception e) {
                    sb.append(",\"relics\":{}");
                }
                try {
                    MuseumData museum = MuseumData.get(level);
                    sb.append(",\"museum\":{\"items\":").append(museum.getDonatedItems(targetId).size());
                    sb.append(",\"mobs\":").append(museum.getDonatedMobs(targetId).size());
                    sb.append(",\"art\":").append(museum.getDonatedArt(targetId).size());
                    sb.append(",\"achievements\":").append(museum.getCompletedAchievements(targetId).size()).append("}");
                } catch (Exception e) {
                    sb.append(",\"museum\":{\"items\":0,\"mobs\":0,\"art\":0,\"achievements\":0}");
                }
                PlayerStatistics stats = PlayerStatistics.get(level);
                sb.append(",\"stats\":{\"kills\":").append(stats.getStat(targetId, "kills"));
                sb.append(",\"deaths\":").append(stats.getStat(targetId, "deaths"));
                sb.append(",\"mobKills\":").append(stats.getStat(targetId, "mobKills"));
                sb.append(",\"blocksBroken\":").append(stats.getStat(targetId, "blocksBroken"));
                sb.append(",\"blocksPlaced\":").append(stats.getStat(targetId, "blocksPlaced"));
                sb.append(",\"playTimeTicks\":").append(stats.getStat(targetId, "playTimeTicks")).append("}");
                try {
                    DungeonManager dm = DungeonManager.get(level);
                    DungeonManager.DungeonInstance di = dm.getDungeonForPlayer(targetId);
                    if (di != null) {
                        sb.append(",\"inDungeon\":true");
                        sb.append(",\"dungeonTier\":\"").append(di.tier.name()).append("\"");
                        sb.append(",\"dungeonTheme\":\"").append(di.theme.name()).append("\"");
                    } else {
                        sb.append(",\"inDungeon\":false");
                    }
                } catch (Exception e) {
                    sb.append(",\"inDungeon\":false");
                }
                // Prestige
                try {
                    com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                    sb.append(",\"prestige\":{");
                    first = true;
                    for (SkillTreeType tree : SkillTreeType.values()) {
                        if (!first) sb.append(",");
                        first = false;
                        sb.append("\"").append(tree.name()).append("\":").append(prestige.getPrestigeLevel(targetId, tree));
                    }
                    sb.append(",\"total\":").append(prestige.getTotalPrestige(targetId));
                    sb.append("}");
                } catch (Exception e) { sb.append(",\"prestige\":{}"); }
                // Mastery Marks
                try {
                    com.ultra.megamod.feature.prestige.MasteryMarkManager marks = com.ultra.megamod.feature.prestige.MasteryMarkManager.get(level);
                    sb.append(",\"masteryMarks\":").append(marks.getMarks(targetId));
                } catch (Exception e) { sb.append(",\"masteryMarks\":0"); }
                // Arena stats
                try {
                    com.ultra.megamod.feature.arena.ArenaManager arena = com.ultra.megamod.feature.arena.ArenaManager.get(level);
                    sb.append(",\"arena\":{");
                    sb.append("\"elo\":").append(arena.getEloRating(targetId));
                    sb.append(",\"pvpWins\":").append(arena.getPvpWins(targetId));
                    sb.append(",\"pvpLosses\":").append(arena.getPvpLosses(targetId));
                    sb.append(",\"bestWave\":").append(arena.getBestPveWave(targetId));
                    sb.append(",\"bestBossRush\":").append(arena.getBestBossRushTime(targetId));
                    sb.append("}");
                } catch (Exception e) { sb.append(",\"arena\":{}"); }
                // New Game+ progress
                try {
                    com.ultra.megamod.feature.dungeons.NewGamePlusManager ngp = com.ultra.megamod.feature.dungeons.NewGamePlusManager.get(level);
                    sb.append(",\"ngplus\":{");
                    sb.append("\"mythicAccess\":").append(ngp.canAccessMythic(targetId, level));
                    sb.append(",\"eternalAccess\":").append(ngp.canAccessEternal(targetId, level));
                    int infernalBosses = ngp.getDefeatedBosses(targetId, "INFERNAL").size();
                    int mythicBosses = ngp.getDefeatedBosses(targetId, "MYTHIC").size();
                    sb.append(",\"infernalBosses\":").append(infernalBosses);
                    sb.append(",\"mythicBosses\":").append(mythicBosses);
                    sb.append("}");
                } catch (Exception e) { sb.append(",\"ngplus\":{}"); }
                // Inventory
                sb.append(",\"inventory\":[");
                boolean firstInv = true;
                if (target != null) {
                    for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = target.getInventory().getItem(i);
                        if (invStack.isEmpty()) continue;
                        if (!firstInv) sb.append(",");
                        firstInv = false;
                        sb.append("{\"slot\":").append(i);
                        sb.append(",\"id\":\"").append(BuiltInRegistries.ITEM.getKey(invStack.getItem()).toString()).append("\"");
                        sb.append(",\"count\":").append(invStack.getCount());
                        sb.append(",\"name\":\"").append(invStack.getHoverName().getString().replace("\"", "\\\"")).append("\"");
                        sb.append("}");
                    }
                }
                sb.append("]");
                sb.append("}");
                ComputerActionHandler.sendResponse(player, "player_detail_data", sb.toString(), eco);
                break;
            }
            case "admin_clear_player_inv": {
                try {
                    UUID targetId2 = UUID.fromString(jsonData);
                    ServerPlayer target2 = level.getServer().getPlayerList().getPlayer(targetId2);
                    if (target2 != null) {
                        target2.getInventory().clearContent();
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Cleared inventory for " + target2.getGameProfile().name() + "\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Player not online\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Error clearing inventory\"}", eco);
                }
                break;
            }
            case "admin_give_player_item": {
                try {
                    // Format: uuid|itemId|count (using | as delimiter to avoid UUID colon conflicts)
                    String[] parts = jsonData.split("\\|");
                    if (parts.length < 3) return;
                    UUID targetId2 = UUID.fromString(parts[0]);
                    String itemId = parts[1];
                    int count = Integer.parseInt(parts[2]);
                    ServerPlayer target2 = level.getServer().getPlayerList().getPlayer(targetId2);
                    if (target2 != null) {
                        Item item = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(itemId));
                        ItemStack stack = new ItemStack(item, count);
                        target2.getInventory().add(stack);
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Gave " + count + "x " + itemId + " to " + target2.getGameProfile().name() + "\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Player not online\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Error giving item\"}", eco);
                }
                break;
            }
            case "admin_fill_area": {
                try {
                    // Format: minecraft:block_id:radius:yOffset
                    String[] parts = jsonData.split(":");
                    if (parts.length < 4) return;
                    String blockId = parts[0] + ":" + parts[1];
                    int radius = Math.min(Integer.parseInt(parts[2]), 15);
                    int yOff = Integer.parseInt(parts[3]);
                    String cmd = String.format("fill ~%d ~%d ~%d ~%d ~%d ~%d %s",
                        -radius, yOff, -radius, radius, yOff, radius, blockId);
                    AdminSystem.executeCommand(player, cmd);
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Filled " + blockId + " r=" + radius + " yOff=" + yOff + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Fill error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_replace_area": {
                try {
                    // Format: minecraft:from_block:minecraft:to_block:radius
                    String[] parts = jsonData.split(":");
                    if (parts.length < 5) return;
                    String fromBlock = parts[0] + ":" + parts[1];
                    String toBlock = parts[2] + ":" + parts[3];
                    int radius = Math.min(Integer.parseInt(parts[4]), 15);
                    String cmd = String.format("fill ~%d ~-1 ~%d ~%d ~3 ~%d %s replace %s",
                        -radius, -radius, radius, radius, toBlock, fromBlock);
                    AdminSystem.executeCommand(player, cmd);
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Replaced " + fromBlock + " -> " + toBlock + " r=" + radius + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Replace error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_kick": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target != null) {
                    target.connection.disconnect((Component)Component.literal((String)"Kicked by admin"));
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Kicked " + jsonData + "\"}", eco);
                } else {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Player not found\"}", eco);
                }
                break;
            }
            case "admin_ban": {
                String result = AdminSystem.executeCommand(player, "ban " + jsonData);
                String escaped = result.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"" + escaped + "\"}", eco);
                break;
            }
            case "admin_mute": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target != null) {
                    AdminSystem.mute(target.getUUID());
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Muted " + jsonData + "\"}", eco);
                } else {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Player not found\"}", eco);
                }
                break;
            }
            case "admin_unmute": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target != null) {
                    AdminSystem.unmute(target.getUUID());
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Unmuted " + jsonData + "\"}", eco);
                } else {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Player not found\"}", eco);
                }
                break;
            }
            case "request_dungeons": {
                try {
                    DungeonManager dm = DungeonManager.get(level);
                    Map<String, DungeonManager.DungeonInstance> all = dm.getAllInstances();
                    StringBuilder sb = new StringBuilder("{\"instances\":[");
                    boolean first = true;
                    for (Map.Entry<String, DungeonManager.DungeonInstance> entry : all.entrySet()) {
                        DungeonManager.DungeonInstance di = entry.getValue();
                        if (!first) sb.append(",");
                        first = false;
                        String pName = ComputerActionHandler.resolveName(di.playerUUID, level);
                        sb.append("{\"id\":\"").append(di.instanceId).append("\"");
                        sb.append(",\"player\":\"").append(pName.replace("\"", "\\\"")).append("\"");
                        sb.append(",\"uuid\":\"").append(di.playerUUID).append("\"");
                        sb.append(",\"tier\":\"").append(di.tier.name()).append("\"");
                        sb.append(",\"theme\":\"").append(di.theme.name()).append("\"");
                        sb.append(",\"rooms\":").append(di.roomsCleared);
                        sb.append(",\"totalRooms\":").append(di.totalRooms);
                        sb.append(",\"bossAlive\":").append(di.bossAlive);
                        sb.append(",\"cleared\":").append(di.cleared);
                        sb.append(",\"abandoned\":").append(di.abandoned);
                        sb.append("}");
                    }
                    sb.append("]}");
                    ComputerActionHandler.sendResponse(player, "dungeons_data", sb.toString(), eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "dungeons_data", "{\"instances\":[]}", eco);
                }
                break;
            }
            case "dungeon_force_extract": {
                try {
                    UUID targetId = UUID.fromString(jsonData);
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target != null) {
                        DungeonManager dm = DungeonManager.get(level);
                        dm.removePlayerFromDungeon(target);
                    }
                } catch (Exception e) {}
                ComputerActionHandler.handleAction(player, "request_dungeons", "");
                break;
            }
            case "request_warps": {
                AdminWarpManager wm = AdminWarpManager.get(level);
                Map<String, AdminWarpManager.WarpPoint> warps = wm.getAllWarps();
                StringBuilder sb = new StringBuilder("{\"warps\":[");
                boolean first = true;
                for (Map.Entry<String, AdminWarpManager.WarpPoint> entry : warps.entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    AdminWarpManager.WarpPoint wp = entry.getValue();
                    sb.append("{\"name\":\"").append(entry.getKey().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"x\":").append((int) wp.x());
                    sb.append(",\"y\":").append((int) wp.y());
                    sb.append(",\"z\":").append((int) wp.z());
                    sb.append(",\"dim\":\"").append(wp.dimension()).append("\"}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "warps_data", sb.toString(), eco);
                break;
            }
            case "warp_save": {
                AdminWarpManager wm = AdminWarpManager.get(level);
                wm.saveWarp(jsonData, player);
                wm.saveToDisk(level);
                ComputerActionHandler.handleAction(player, "request_warps", "");
                break;
            }
            case "warp_delete": {
                AdminWarpManager wm = AdminWarpManager.get(level);
                wm.deleteWarp(jsonData);
                wm.saveToDisk(level);
                ComputerActionHandler.handleAction(player, "request_warps", "");
                break;
            }
            case "warp_teleport": {
                AdminWarpManager wm = AdminWarpManager.get(level);
                AdminWarpManager.WarpPoint wp = wm.getWarp(jsonData);
                if (wp != null) {
                    player.teleportTo(level.getServer().overworld(), wp.x(), wp.y(), wp.z(), Set.of(), wp.yRot(), wp.xRot(), false);
                    ComputerActionHandler.sendResponse(player, "warp_result", "{\"success\":true}", eco);
                } else {
                    ComputerActionHandler.sendResponse(player, "warp_result", "{\"success\":false}", eco);
                }
                break;
            }
            case "admin_broadcast": {
                for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                    p.sendSystemMessage((Component)Component.literal((String)("[Broadcast] " + jsonData)).withStyle(ChatFormatting.GOLD));
                }
                ComputerActionHandler.sendResponse(player, "broadcast_result", "{\"success\":true}", eco);
                break;
            }
            case "request_audit_log": {
                List<EconomyManager.AuditEntry> log = eco.getAuditLog();
                StringBuilder sb = new StringBuilder("{\"entries\":[");
                boolean first = true;
                for (EconomyManager.AuditEntry entry : log) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"time\":").append(entry.timestamp());
                    sb.append(",\"player\":\"").append(entry.playerName().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"type\":\"").append(entry.type()).append("\"");
                    sb.append(",\"amount\":").append(entry.amount());
                    sb.append(",\"desc\":\"").append(entry.description().replace("\"", "\\\"")).append("\"}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "audit_log_data", sb.toString(), eco);
                break;
            }
            case "request_player_audit": {
                com.ultra.megamod.feature.audit.AuditLogManager audit = com.ultra.megamod.feature.audit.AuditLogManager.get(level);
                List<com.ultra.megamod.feature.audit.AuditLogManager.AuditEntry> entries = audit.getEntries();
                StringBuilder sb = new StringBuilder("{\"entries\":[");
                boolean first = true;
                for (com.ultra.megamod.feature.audit.AuditLogManager.AuditEntry entry : entries) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"time\":").append(entry.timestamp());
                    sb.append(",\"player\":\"").append(entry.playerName().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"type\":\"").append(entry.eventType().name()).append("\"");
                    sb.append(",\"desc\":\"").append(entry.description().replace("\"", "\\\"")).append("\"}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "player_audit_data", sb.toString(), eco);
                break;
            }
            case "request_inventory": {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.isEmpty()) continue;
                    boolean hasWeapon = WeaponStatRoller.isWeaponInitialized(stack);
                    boolean hasArmor = com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack);
                    boolean hasRelic = RelicData.isInitialized(stack);
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"slot\":").append(i);
                    sb.append(",\"itemId\":\"").append(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).append("\"");
                    sb.append(",\"displayName\":\"").append(stack.getHoverName().getString().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"hasWeaponStats\":").append(hasWeapon || hasArmor);
                    if (hasArmor && !hasWeapon) {
                        // For armor, populate weapon-like fields from armor data
                        WeaponRarity rar = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getRarity(stack);
                        sb.append(",\"rarityName\":\"").append(rar.getDisplayName()).append("\"");
                        sb.append(",\"rarityColor\":").append(rar.getNameColor().getColor());
                        sb.append(",\"baseDamage\":").append(com.ultra.megamod.feature.relics.data.ArmorStatRoller.getStoredBaseArmor(stack));
                        CompoundTag armorTag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                        CompoundTag armorBonuses = armorTag.getCompoundOrEmpty("armor_rolled_bonuses");
                        sb.append(",\"bonusCount\":").append(armorBonuses.getIntOr("count", 0));
                        sb.append(",\"isArmor\":true");
                    }
                    if (hasWeapon) {
                        WeaponRarity rar = WeaponStatRoller.getRarity(stack);
                        sb.append(",\"rarityName\":\"").append(rar.getDisplayName()).append("\"");
                        sb.append(",\"rarityColor\":").append(rar.getNameColor().getColor());
                        sb.append(",\"baseDamage\":").append(WeaponStatRoller.getStoredBaseDamage(stack));
                        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                        CompoundTag bonuses = tag.getCompoundOrEmpty("weapon_rolled_bonuses");
                        int bonusCount = bonuses.getIntOr("count", 0);
                        sb.append(",\"bonusCount\":").append(bonusCount);
                        sb.append(",\"bonuses\":[");
                        for (int bi = 0; bi < bonusCount; bi++) {
                            CompoundTag bEntry = bonuses.getCompoundOrEmpty("bonus_" + bi);
                            if (bi > 0) sb.append(",");
                            sb.append("{\"name\":\"").append(bEntry.getStringOr("name", "Unknown").replace("\"", "\\\"")).append("\"");
                            sb.append(",\"value\":").append(bEntry.getDoubleOr("value", 0.0));
                            sb.append(",\"percent\":").append(bEntry.getBooleanOr("percent", false));
                            sb.append(",\"attr\":\"").append(bEntry.getStringOr("attr", "").replace("\"", "\\\"")).append("\"");
                            sb.append("}");
                        }
                        sb.append("]");
                        // Phase H: manual RPG weapon skills removed — tomes cast via SpellEngine.
                        sb.append(",\"weaponSkills\":[]");
                    }
                    sb.append(",\"isRelicItem\":").append(stack.getItem() instanceof RelicItem);
                    sb.append(",\"hasRelicData\":").append(hasRelic);
                    if (hasRelic) {
                        sb.append(",\"relicLevel\":").append(RelicData.getLevel(stack));
                        sb.append(",\"relicQuality\":").append(RelicData.getQuality(stack));
                        sb.append(",\"relicXp\":").append(RelicData.getXp(stack));
                        // Include ability details for relic items
                        if (stack.getItem() instanceof RelicItem relicItem) {
                            sb.append(",\"abilities\":[");
                            boolean firstAbility = true;
                            for (com.ultra.megamod.feature.relics.data.RelicAbility ability : relicItem.getAbilities()) {
                                if (!firstAbility) sb.append(",");
                                firstAbility = false;
                                sb.append("{\"name\":\"").append(ability.name().replace("\"", "\\\"")).append("\"");
                                sb.append(",\"desc\":\"").append(ability.description().replace("\"", "\\\"")).append("\"");
                                sb.append(",\"reqLevel\":").append(ability.requiredLevel());
                                sb.append(",\"castType\":\"").append(ability.castType().name()).append("\"");
                                int pts = RelicData.getAbilityPoints(stack, ability.name());
                                sb.append(",\"points\":").append(pts);
                                int defaultCd = ability.castType() == com.ultra.megamod.feature.relics.data.RelicAbility.CastType.INSTANTANEOUS ? 60 :
                                                ability.castType() == com.ultra.megamod.feature.relics.data.RelicAbility.CastType.TOGGLE ? 20 : 0;
                                int effectiveCd = RelicData.getEffectiveCooldown(stack, ability.name(), defaultCd);
                                sb.append(",\"cooldownTicks\":").append(effectiveCd);
                                sb.append(",\"defaultCooldown\":").append(defaultCd);
                                sb.append(",\"stats\":[");
                                boolean firstStat = true;
                                for (com.ultra.megamod.feature.relics.data.RelicStat stat : ability.stats()) {
                                    if (!firstStat) sb.append(",");
                                    firstStat = false;
                                    double baseVal = RelicData.getStatBaseValue(stack, ability.name(), stat.name());
                                    double computed = RelicData.getComputedStatValue(stack, ability.name(), stat);
                                    sb.append("{\"name\":\"").append(stat.name().replace("\"", "\\\"")).append("\"");
                                    sb.append(",\"base\":").append(baseVal);
                                    sb.append(",\"computed\":").append(String.format("%.2f", computed));
                                    sb.append(",\"min\":").append(stat.minValue());
                                    sb.append(",\"max\":").append(stat.maxValue());
                                    sb.append("}");
                                }
                                sb.append("]}");
                            }
                            sb.append("]");
                        }
                    }
                    // Enchantments
                    sb.append(",\"enchantments\":[");
                    boolean firstEnch = true;
                    try {
                        net.minecraft.world.item.enchantment.ItemEnchantments enchants = stack.getEnchantments();
                        for (var enchHolder : enchants.keySet()) {
                            if (!firstEnch) sb.append(",");
                            firstEnch = false;
                            String enchId = enchHolder.unwrapKey().map(k -> k.identifier().toString()).orElse("unknown");
                            int enchLvl = enchants.getLevel(enchHolder);
                            String enchName = enchId.substring(enchId.indexOf(':') + 1).replace('_', ' ');
                            sb.append("{\"id\":\"").append(enchId.replace("\"", "\\\"")).append("\"");
                            sb.append(",\"name\":\"").append(enchName.replace("\"", "\\\"")).append("\"");
                            sb.append(",\"level\":").append(enchLvl).append("}");
                        }
                    } catch (Exception ignored) {}
                    sb.append("]");

                    // Custom name
                    String customNameStr = "";
                    if (stack.has(DataComponents.CUSTOM_NAME)) {
                        net.minecraft.network.chat.Component nameComp = stack.get(DataComponents.CUSTOM_NAME);
                        if (nameComp != null) customNameStr = nameComp.getString().replace("\"", "\\\"");
                    }
                    sb.append(",\"customName\":\"").append(customNameStr).append("\"");

                    // Lore lines
                    sb.append(",\"lore\":[");
                    boolean firstLore = true;
                    try {
                        net.minecraft.world.item.component.ItemLore lore = stack.get(DataComponents.LORE);
                        if (lore != null) {
                            for (net.minecraft.network.chat.Component line : lore.lines()) {
                                if (!firstLore) sb.append(",");
                                firstLore = false;
                                sb.append("\"").append(line.getString().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
                            }
                        }
                    } catch (Exception ignored) {}
                    sb.append("]");

                    sb.append("}");
                }
                sb.append("]");
                ComputerActionHandler.sendResponse(player, "inventory_data", sb.toString(), eco);
                break;
            }
            case "research_reroll": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    if (WeaponStatRoller.isWeaponInitialized(stack)) {
                        WeaponStatRoller.rerollBonuses(stack, level.random);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Rerolled weapon bonuses for slot " + slot + "\"}", eco);
                    } else if (com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack)) {
                        com.ultra.megamod.feature.relics.data.ArmorStatRoller.rerollBonuses(stack, level.random);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Rerolled armor bonuses for slot " + slot + "\"}", eco);
                    }
                }
                break;
            }
            case "research_max_rarity": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    if (WeaponStatRoller.isWeaponInitialized(stack)) {
                        float baseDmg = WeaponStatRoller.getStoredBaseDamage(stack) / WeaponStatRoller.getRarity(stack).getDamageMultiplier();
                        WeaponStatRoller.rollAndApply(stack, baseDmg, WeaponRarity.LEGENDARY, level.random, WeaponStatRoller.isStoredShield(stack));
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Set to Legendary rarity\"}", eco);
                    } else if (com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack)) {
                        double baseArmor = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getStoredBaseArmor(stack) / (1.0 + com.ultra.megamod.feature.relics.data.ArmorStatRoller.getRarity(stack).ordinal() * 0.15);
                        net.minecraft.world.entity.EquipmentSlot eqSlot = net.minecraft.world.entity.EquipmentSlot.CHEST;
                        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                        if (equippable != null) eqSlot = equippable.slot();
                        com.ultra.megamod.feature.relics.data.ArmorStatRoller.rollAndApply(stack, baseArmor, baseArmor * 0.2, eqSlot, WeaponRarity.LEGENDARY, level.random);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Set armor to Legendary rarity\"}", eco);
                    }
                }
                break;
            }
            case "research_rarity_up": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    if (WeaponStatRoller.isWeaponInitialized(stack)) {
                        WeaponRarity current = WeaponStatRoller.getRarity(stack);
                        int nextOrd = Math.min(current.ordinal() + 1, WeaponRarity.values().length - 1);
                        WeaponRarity next = WeaponRarity.fromOrdinal(nextOrd);
                        float baseDmg = WeaponStatRoller.getStoredBaseDamage(stack) / current.getDamageMultiplier();
                        WeaponStatRoller.rollAndApply(stack, baseDmg, next, level.random, WeaponStatRoller.isStoredShield(stack));
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Rarity -> " + next.getDisplayName() + "\"}", eco);
                    } else if (com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack)) {
                        WeaponRarity current = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getRarity(stack);
                        int nextOrd = Math.min(current.ordinal() + 1, WeaponRarity.values().length - 1);
                        WeaponRarity next = WeaponRarity.fromOrdinal(nextOrd);
                        double baseArmor = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getStoredBaseArmor(stack) / (1.0 + current.ordinal() * 0.15);
                        net.minecraft.world.entity.EquipmentSlot eqSlot = net.minecraft.world.entity.EquipmentSlot.CHEST;
                        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                        if (equippable != null) eqSlot = equippable.slot();
                        com.ultra.megamod.feature.relics.data.ArmorStatRoller.rollAndApply(stack, baseArmor, baseArmor * 0.2, eqSlot, next, level.random);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Armor rarity -> " + next.getDisplayName() + "\"}", eco);
                    }
                }
                break;
            }
            case "research_rarity_down": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    if (WeaponStatRoller.isWeaponInitialized(stack)) {
                        WeaponRarity current = WeaponStatRoller.getRarity(stack);
                        int prevOrd = Math.max(current.ordinal() - 1, 0);
                        WeaponRarity prev = WeaponRarity.fromOrdinal(prevOrd);
                        float baseDmg = WeaponStatRoller.getStoredBaseDamage(stack) / current.getDamageMultiplier();
                        WeaponStatRoller.rollAndApply(stack, baseDmg, prev, level.random, WeaponStatRoller.isStoredShield(stack));
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Rarity -> " + prev.getDisplayName() + "\"}", eco);
                    } else if (com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack)) {
                        WeaponRarity current = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getRarity(stack);
                        int prevOrd = Math.max(current.ordinal() - 1, 0);
                        WeaponRarity prev = WeaponRarity.fromOrdinal(prevOrd);
                        double baseArmor = com.ultra.megamod.feature.relics.data.ArmorStatRoller.getStoredBaseArmor(stack) / (1.0 + current.ordinal() * 0.15);
                        net.minecraft.world.entity.EquipmentSlot eqSlot = net.minecraft.world.entity.EquipmentSlot.CHEST;
                        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                        if (equippable != null) eqSlot = equippable.slot();
                        com.ultra.megamod.feature.relics.data.ArmorStatRoller.rollAndApply(stack, baseArmor, baseArmor * 0.2, eqSlot, prev, level.random);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Armor rarity -> " + prev.getDisplayName() + "\"}", eco);
                    }
                }
                break;
            }
            case "research_max_relic": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setLevel(stack, 10);
                    RelicData.setQuality(stack, 10);
                    RelicData.setXp(stack, 0);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic maxed: Lv10, Q10\"}", eco);
                }
                break;
            }
            case "research_set_bonus": {
                String[] parts = jsonData.split(":");
                if (parts.length < 3) return;
                int slot = Integer.parseInt(parts[0]);
                int bonusIdx = Integer.parseInt(parts[1]);
                double newValue = Double.parseDouble(parts[2]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && WeaponStatRoller.isWeaponInitialized(stack)) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    CompoundTag bonuses = tag.getCompoundOrEmpty("weapon_rolled_bonuses");
                    CompoundTag entry = bonuses.getCompoundOrEmpty("bonus_" + bonusIdx);
                    if (!entry.isEmpty()) {
                        entry.putDouble("value", newValue);
                        bonuses.put("bonus_" + bonusIdx, entry);
                        tag.put("weapon_rolled_bonuses", bonuses);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        WeaponStatRoller.rebuildModifiersFromTag(stack);
                    }
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Bonus " + bonusIdx + " set to " + String.format("%.2f", newValue) + "\"}", eco);
                }
                break;
            }
            case "research_add_bonus": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && WeaponStatRoller.isWeaponInitialized(stack)) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    CompoundTag bonuses = tag.getCompoundOrEmpty("weapon_rolled_bonuses");
                    int count = bonuses.getIntOr("count", 0);
                    WeaponRarity rar = WeaponStatRoller.getRarity(stack);
                    java.util.List<WeaponStatRoller.BonusStat> pool = WeaponStatRoller.BONUS_POOL;
                    WeaponStatRoller.BonusStat chosen = pool.get(level.random.nextInt(pool.size()));
                    double value = chosen.roll(level.random, rar);
                    CompoundTag newEntry = new CompoundTag();
                    newEntry.putString("name", chosen.displayName());
                    newEntry.putString("attr", chosen.attributeId());
                    newEntry.putDouble("value", value);
                    newEntry.putBoolean("percent", chosen.isPercent());
                    newEntry.putInt("op", chosen.operation().ordinal());
                    bonuses.put("bonus_" + count, newEntry);
                    bonuses.putInt("count", count + 1);
                    tag.put("weapon_rolled_bonuses", bonuses);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    WeaponStatRoller.rebuildModifiersFromTag(stack);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Added bonus: " + chosen.displayName() + "\"}", eco);
                }
                break;
            }
            case "research_remove_bonus": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int slot = Integer.parseInt(parts[0]);
                int bonusIdx = Integer.parseInt(parts[1]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && WeaponStatRoller.isWeaponInitialized(stack)) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    CompoundTag bonuses = tag.getCompoundOrEmpty("weapon_rolled_bonuses");
                    int count = bonuses.getIntOr("count", 0);
                    if (bonusIdx < count) {
                        for (int i = bonusIdx; i < count - 1; i++) {
                            bonuses.put("bonus_" + i, bonuses.getCompoundOrEmpty("bonus_" + (i + 1)));
                        }
                        bonuses.remove("bonus_" + (count - 1));
                        bonuses.putInt("count", count - 1);
                        tag.put("weapon_rolled_bonuses", bonuses);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        WeaponStatRoller.rebuildModifiersFromTag(stack);
                    }
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Removed bonus " + bonusIdx + "\"}", eco);
                }
                break;
            }
            case "research_set_relic_level": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int slot = Integer.parseInt(parts[0]);
                int newLevel = Integer.parseInt(parts[1]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setLevel(stack, newLevel);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic level set to " + newLevel + "\"}", eco);
                }
                break;
            }
            case "research_set_relic_quality": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int slot = Integer.parseInt(parts[0]);
                int newQuality = Integer.parseInt(parts[1]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setQuality(stack, newQuality);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic quality set to " + newQuality + "\"}", eco);
                }
                break;
            }
            case "research_init_relic": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof RelicItem relicItem && !RelicData.isInitialized(stack)) {
                    RelicData.initialize(stack, relicItem.getAbilities(), level.random);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic initialized\"}", eco);
                }
                break;
            }
            case "research_set_relic_xp": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int slot = Integer.parseInt(parts[0]);
                int newXp = Integer.parseInt(parts[1]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setXp(stack, Math.max(0, newXp));
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic XP set to " + Math.max(0, newXp) + "\"}", eco);
                }
                break;
            }
            case "research_reroll_relic_stats": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack) && stack.getItem() instanceof RelicItem relicItem) {
                    for (com.ultra.megamod.feature.relics.data.RelicAbility ability : relicItem.getAbilities()) {
                        for (com.ultra.megamod.feature.relics.data.RelicStat stat : ability.stats()) {
                            RelicData.rerollStat(stack, ability.name(), stat, level.random);
                        }
                    }
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"All relic stats rerolled\"}", eco);
                }
                break;
            }
            case "research_max_relic_stats": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack) && stack.getItem() instanceof RelicItem relicItem) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    CompoundTag statsTag = tag.getCompoundOrEmpty("relic_stats");
                    for (com.ultra.megamod.feature.relics.data.RelicAbility ability : relicItem.getAbilities()) {
                        CompoundTag abilityTag = statsTag.getCompoundOrEmpty(ability.name());
                        for (com.ultra.megamod.feature.relics.data.RelicStat stat : ability.stats()) {
                            abilityTag.putDouble(stat.name(), stat.maxValue());
                        }
                        statsTag.put(ability.name(), abilityTag);
                    }
                    tag.put("relic_stats", statsTag);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"All relic stats maxed\"}", eco);
                }
                break;
            }
            case "research_reset_relic": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    tag.remove("relic_level");
                    tag.remove("relic_xp");
                    tag.remove("relic_quality");
                    tag.remove("relic_initialized");
                    tag.remove("relic_stats");
                    tag.remove("relic_ability_points");
                    tag.remove("relic_exchanges");
                    tag.remove("ability_cooldown_overrides");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Relic data reset\"}", eco);
                }
                break;
            }
            case "research_init_weapon": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && !WeaponStatRoller.isWeaponInitialized(stack)) {
                    boolean isShield = stack.getItem() instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem rpg && rpg.isShield();
                    WeaponStatRoller.rollAndApply(stack, 5.0f, level.random, isShield);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Initialized weapon stats\"}", eco);
                }
                break;
            }
            case "research_init_armor": {
                int slot = Integer.parseInt(jsonData);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && !com.ultra.megamod.feature.relics.data.ArmorStatRoller.isArmorInitialized(stack)) {
                    // Determine equipment slot from inventory position or item type
                    net.minecraft.world.entity.EquipmentSlot eqSlot = net.minecraft.world.entity.EquipmentSlot.CHEST;
                    net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                    if (equippable != null) {
                        eqSlot = equippable.slot();
                    }
                    com.ultra.megamod.feature.relics.data.ArmorStatRoller.rollAndApply(stack, 5.0, 1.0, eqSlot, level.random);
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Initialized armor stats\"}", eco);
                }
                break;
            }
            case "research_add_specific_bonus": {
                String[] parts = jsonData.split(":");
                if (parts.length < 2) return;
                int slot = Integer.parseInt(parts[0]);
                int poolIndex = Integer.parseInt(parts[1]);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && WeaponStatRoller.isWeaponInitialized(stack)) {
                    // Extended bonus pool covering all custom attributes
                    java.util.List<WeaponStatRoller.BonusStat> extPool = java.util.List.of(
                        // Original 16 from BONUS_POOL
                        new WeaponStatRoller.BonusStat("minecraft:attack_damage", 0.5, 3.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Attack Damage", false),
                        new WeaponStatRoller.BonusStat("minecraft:attack_speed", 0.05, 0.3, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Attack Speed", false),
                        new WeaponStatRoller.BonusStat("minecraft:max_health", 1.0, 6.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Max Health", false),
                        new WeaponStatRoller.BonusStat("minecraft:movement_speed", 0.01, 0.04, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "Movement Speed", true),
                        new WeaponStatRoller.BonusStat("minecraft:armor", 1.0, 4.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Armor", false),
                        new WeaponStatRoller.BonusStat("minecraft:armor_toughness", 0.5, 2.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Armor Toughness", false),
                        new WeaponStatRoller.BonusStat("minecraft:luck", 0.5, 2.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Luck", false),
                        new WeaponStatRoller.BonusStat("megamod:critical_chance", 2.0, 15.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Critical Chance", true),
                        new WeaponStatRoller.BonusStat("megamod:critical_damage", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Critical Damage", true),
                        new WeaponStatRoller.BonusStat("megamod:lifesteal", 1.0, 8.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Lifesteal", true),
                        new WeaponStatRoller.BonusStat("megamod:fire_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Fire Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:ice_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Ice Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:lightning_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Lightning Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:cooldown_reduction", 2.0, 15.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Cooldown Reduction", true),
                        new WeaponStatRoller.BonusStat("megamod:dodge_chance", 1.0, 5.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Dodge Chance", true),
                        new WeaponStatRoller.BonusStat("megamod:health_regen_bonus", 0.5, 2.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Health Regen", false),
                        // Additional custom attributes
                        new WeaponStatRoller.BonusStat("megamod:poison_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Poison Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:holy_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Holy Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:shadow_damage_bonus", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Shadow Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:thorns_damage", 1.0, 5.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Thorns Damage", false),
                        new WeaponStatRoller.BonusStat("megamod:armor_shred", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Armor Shred", false),
                        new WeaponStatRoller.BonusStat("megamod:stun_chance", 1.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Stun Chance", true),
                        new WeaponStatRoller.BonusStat("megamod:ability_power", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Ability Power", false),
                        new WeaponStatRoller.BonusStat("megamod:mana_efficiency", 2.0, 15.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Mana Efficiency", true),
                        new WeaponStatRoller.BonusStat("megamod:spell_range", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Spell Range", false),
                        new WeaponStatRoller.BonusStat("megamod:combo_speed", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Combo Speed", false),
                        new WeaponStatRoller.BonusStat("megamod:fire_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Fire Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:ice_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Ice Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:lightning_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Lightning Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:poison_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Poison Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:holy_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Holy Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:shadow_resistance_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Shadow Resist", true),
                        new WeaponStatRoller.BonusStat("megamod:mining_speed_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Mining Speed", false),
                        new WeaponStatRoller.BonusStat("megamod:swim_speed_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Swim Speed", false),
                        new WeaponStatRoller.BonusStat("megamod:jump_height_bonus", 0.5, 2.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Jump Height", false),
                        new WeaponStatRoller.BonusStat("megamod:fall_damage_reduction", 5.0, 25.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Fall Reduction", true),
                        new WeaponStatRoller.BonusStat("megamod:hunger_efficiency", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Hunger Eff.", true),
                        new WeaponStatRoller.BonusStat("megamod:megacoin_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Coin Bonus", true),
                        new WeaponStatRoller.BonusStat("megamod:shop_discount", 2.0, 10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Shop Discount", true),
                        new WeaponStatRoller.BonusStat("megamod:sell_bonus", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Sell Bonus", true),
                        new WeaponStatRoller.BonusStat("megamod:loot_fortune", 5.0, 20.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Loot Fortune", true),
                        new WeaponStatRoller.BonusStat("megamod:xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "XP Bonus", true),
                        new WeaponStatRoller.BonusStat("megamod:combat_xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Combat XP", true),
                        new WeaponStatRoller.BonusStat("megamod:mining_xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Mining XP", true),
                        new WeaponStatRoller.BonusStat("megamod:farming_xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Farming XP", true),
                        new WeaponStatRoller.BonusStat("megamod:arcane_xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Arcane XP", true),
                        new WeaponStatRoller.BonusStat("megamod:survival_xp_bonus", 5.0, 30.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, "Survival XP", true)
                    );
                    if (poolIndex >= 0 && poolIndex < extPool.size()) {
                        WeaponStatRoller.BonusStat chosen = extPool.get(poolIndex);
                        WeaponRarity rar = WeaponStatRoller.getRarity(stack);
                        double value = chosen.roll(level.random, rar);
                        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                        CompoundTag bonuses = tag.getCompoundOrEmpty("weapon_rolled_bonuses");
                        int count = bonuses.getIntOr("count", 0);
                        CompoundTag newEntry = new CompoundTag();
                        newEntry.putString("name", chosen.displayName());
                        newEntry.putString("attr", chosen.attributeId());
                        newEntry.putDouble("value", value);
                        newEntry.putBoolean("percent", chosen.isPercent());
                        newEntry.putInt("op", chosen.operation().ordinal());
                        bonuses.put("bonus_" + count, newEntry);
                        bonuses.putInt("count", count + 1);
                        tag.put("weapon_rolled_bonuses", bonuses);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        WeaponStatRoller.rebuildModifiersFromTag(stack);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Added: " + chosen.displayName() + "\"}", eco);
                    }
                }
                break;
            }
            case "research_set_relic_stat": {
                // format: slot:abilityName:statName:newValue
                int firstColon = jsonData.indexOf(':');
                int secondColon = jsonData.indexOf(':', firstColon + 1);
                int thirdColon = jsonData.indexOf(':', secondColon + 1);
                int slot = Integer.parseInt(jsonData.substring(0, firstColon));
                String abilityName = jsonData.substring(firstColon + 1, secondColon);
                String statName = jsonData.substring(secondColon + 1, thirdColon);
                double newValue = Double.parseDouble(jsonData.substring(thirdColon + 1));
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                    CompoundTag statsTag = tag.getCompoundOrEmpty("relic_stats");
                    CompoundTag abilityTag = statsTag.getCompoundOrEmpty(abilityName);
                    abilityTag.putDouble(statName, newValue);
                    statsTag.put(abilityName, abilityTag);
                    tag.put("relic_stats", statsTag);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"" + statName + " = " + String.format("%.2f", newValue) + "\"}", eco);
                }
                break;
            }
            case "research_set_ability_points": {
                // format: slot:abilityName:points
                int firstColon = jsonData.indexOf(':');
                int secondColon = jsonData.indexOf(':', firstColon + 1);
                int slot = Integer.parseInt(jsonData.substring(0, firstColon));
                String abilityName = jsonData.substring(firstColon + 1, secondColon);
                int points = Integer.parseInt(jsonData.substring(secondColon + 1));
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setAbilityPoints(stack, abilityName, Math.max(0, points));
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"" + abilityName + " points = " + points + "\"}", eco);
                }
                break;
            }
            case "research_set_ability_cooldown": {
                // format: slot:abilityName:ticks
                int firstColon = jsonData.indexOf(':');
                int secondColon = jsonData.indexOf(':', firstColon + 1);
                int slot = Integer.parseInt(jsonData.substring(0, firstColon));
                String abilityName = jsonData.substring(firstColon + 1, secondColon);
                int ticks = Integer.parseInt(jsonData.substring(secondColon + 1));
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && RelicData.isInitialized(stack)) {
                    RelicData.setCooldownOverride(stack, abilityName, Math.max(0, ticks));
                    float seconds = ticks / 20.0f;
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"" + abilityName + " cooldown = " + String.format("%.1f", seconds) + "s (" + ticks + "t)\"}", eco);
                }
                break;
            }
            // NOTE: research_set_weapon_cooldown / research_set_weapon_skill /
            //       research_remove_weapon_skill / research_clear_weapon_skills
            // were deprecated during Phase F (SpellEngine port). Manual weapon-skill
            // overrides are gone — use the Spells tab -> Container Editor instead.
            case "research_add_enchant": {
                // Format: slot:namespace:enchantmentPath:level
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 4) return;
                    int slot = Integer.parseInt(parts[0]);
                    String enchId = parts[1] + ":" + parts[2];
                    int enchLevel = Math.min(255, Math.max(1, Integer.parseInt(parts[3])));
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        var regAccess = level.registryAccess();
                        var enchRegistry = regAccess.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        var enchHolder = enchRegistry.get(net.minecraft.resources.Identifier.parse(enchId));
                        if (enchHolder.isPresent()) {
                            stack.enchant(enchHolder.get(), enchLevel);
                            ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Added " + enchId + " " + enchLevel + "\"}", eco);
                        } else {
                            ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Unknown enchantment: " + enchId + "\"}", eco);
                        }
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Enchant error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "research_remove_enchant": {
                // Format: slot:namespace:enchantmentPath
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 3) return;
                    int slot = Integer.parseInt(parts[0]);
                    String enchId = parts[1] + ":" + parts[2];
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        var regAccess = level.registryAccess();
                        var enchRegistry = regAccess.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        var enchHolder = enchRegistry.get(net.minecraft.resources.Identifier.parse(enchId));
                        if (enchHolder.isPresent()) {
                            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(stack.getEnchantments());
                            mutable.removeIf(h -> h.equals(enchHolder.get()));
                            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                            ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Removed " + enchId + "\"}", eco);
                        }
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Remove enchant error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "research_set_enchant_level": {
                // Format: slot:namespace:enchantmentPath:newLevel
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 4) return;
                    int slot = Integer.parseInt(parts[0]);
                    String enchId = parts[1] + ":" + parts[2];
                    int newLevel2 = Math.min(255, Math.max(0, Integer.parseInt(parts[3])));
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        var regAccess = level.registryAccess();
                        var enchRegistry = regAccess.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        var enchHolder = enchRegistry.get(net.minecraft.resources.Identifier.parse(enchId));
                        if (enchHolder.isPresent()) {
                            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(stack.getEnchantments());
                            mutable.removeIf(h -> h.equals(enchHolder.get()));
                            if (newLevel2 > 0) {
                                mutable.set(enchHolder.get(), newLevel2);
                            }
                            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                            ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"" + enchId + " -> Lv" + newLevel2 + "\"}", eco);
                        }
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Set enchant error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "research_clear_enchants": {
                try {
                    int slot = Integer.parseInt(jsonData);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        stack.set(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"All enchantments cleared\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Clear error\"}", eco);
                }
                break;
            }
            case "research_set_name": {
                try {
                    int sep = jsonData.indexOf(':');
                    int slot = Integer.parseInt(jsonData.substring(0, sep));
                    String name = jsonData.substring(sep + 1);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Name set to: " + name.replace("\"", "\\\"") + "\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Name error\"}", eco);
                }
                break;
            }
            case "research_clear_name": {
                try {
                    int slot = Integer.parseInt(jsonData);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        stack.remove(DataComponents.CUSTOM_NAME);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Custom name cleared\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Clear name error\"}", eco);
                }
                break;
            }
            case "research_add_lore": {
                try {
                    int sep = jsonData.indexOf(':');
                    int slot = Integer.parseInt(jsonData.substring(0, sep));
                    String loreLine = jsonData.substring(sep + 1);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.item.component.ItemLore existing = stack.get(DataComponents.LORE);
                        java.util.List<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>();
                        if (existing != null) {
                            lines.addAll(existing.lines());
                        }
                        lines.add(Component.literal(loreLine));
                        stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Lore line added\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Lore error\"}", eco);
                }
                break;
            }
            case "research_remove_lore": {
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 2) return;
                    int slot = Integer.parseInt(parts[0]);
                    int lineIdx = Integer.parseInt(parts[1]);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.item.component.ItemLore existing = stack.get(DataComponents.LORE);
                        if (existing != null) {
                            java.util.List<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>(existing.lines());
                            if (lineIdx >= 0 && lineIdx < lines.size()) {
                                lines.remove(lineIdx);
                                stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
                                ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Lore line removed\"}", eco);
                            }
                        }
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Remove lore error\"}", eco);
                }
                break;
            }
            case "research_clear_lore": {
                try {
                    int slot = Integer.parseInt(jsonData);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty()) {
                        stack.remove(DataComponents.LORE);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"All lore cleared\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Clear lore error\"}", eco);
                }
                break;
            }
            case "research_set_base_damage": {
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 2) return;
                    int slot = Integer.parseInt(parts[0]);
                    float newDmg = Float.parseFloat(parts[1]);
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty() && WeaponStatRoller.isWeaponInitialized(stack)) {
                        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
                        tag.putFloat("weapon_base_damage", Math.max(0, newDmg));
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        WeaponStatRoller.rebuildModifiersFromTag(stack);
                        ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Base damage -> " + String.format("%.1f", newDmg) + "\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Error: " + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_fill_museum": {
                MuseumData museumData = MuseumData.get(level);
                UUID pid = player.getUUID();
                // Items - use actual ItemCatalog
                for (java.util.List<String> catItems : com.ultra.megamod.feature.museum.catalog.ItemCatalog.ITEMS_BY_CATEGORY.values()) {
                    for (String id : catItems) { museumData.donateItem(pid, id); }
                }
                // Aquarium - use actual AquariumCatalog
                for (com.ultra.megamod.feature.museum.catalog.AquariumCatalog.MobEntry entry : com.ultra.megamod.feature.museum.catalog.AquariumCatalog.ENTRIES) {
                    museumData.donateMob(pid, entry.entityId());
                }
                // Wildlife - use actual WildlifeCatalog
                for (com.ultra.megamod.feature.museum.catalog.WildlifeCatalog.MobEntry entry : com.ultra.megamod.feature.museum.catalog.WildlifeCatalog.ENTRIES) {
                    museumData.donateMob(pid, entry.entityId());
                }
                // Art - use actual ArtCatalog (IDs are bare names like "mona_lisa")
                for (com.ultra.megamod.feature.museum.catalog.ArtCatalog.ArtEntry entry : com.ultra.megamod.feature.museum.catalog.ArtCatalog.ENTRIES) {
                    museumData.donateArt(pid, entry.id());
                }
                // Achievements are NOT filled here — they are earned via advancements
                ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"Museum filled! (Items, Mobs, Art). Achievements earned via advancements.\"}", eco);
                // Refresh the museum if player is in it
                try {
                    com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager.get(level).refreshMuseum(player);
                } catch (Exception ignored) {}
                break;
            }
            case "admin_spawn_admin_weapon": {
                if (!AdminSystem.isAdmin(player)) break;
                // Build admin sword programmatically
                net.minecraft.world.item.ItemStack sword = new net.minecraft.world.item.ItemStack((net.minecraft.world.level.ItemLike) net.minecraft.world.item.Items.NETHERITE_SWORD);
                sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    net.minecraft.network.chat.Component.literal("\u00a76\u00a7lAdmin Sword"));
                sword.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 99999);
                // Enchantments
                net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> enchReg =
                    level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).orElseThrow();
                net.minecraft.world.item.enchantment.ItemEnchantments.Mutable enchBuilder =
                    new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                java.util.Map<String, Integer> swordEnchs = java.util.Map.of(
                    "minecraft:sharpness", 10, "minecraft:fire_aspect", 5, "minecraft:looting", 10,
                    "minecraft:unbreaking", 10, "minecraft:mending", 1, "minecraft:knockback", 5);
                for (var entry : swordEnchs.entrySet()) {
                    enchReg.get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,
                        net.minecraft.resources.Identifier.parse(entry.getKey()))).ifPresent(ench ->
                        enchBuilder.set(ench, entry.getValue()));
                }
                sword.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, enchBuilder.toImmutable());
                player.getInventory().add(sword);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Admin Sword given!\"}", eco);
                break;
            }
            case "admin_spawn_admin_armor": {
                if (!AdminSystem.isAdmin(player)) break;
                net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> aEnchReg =
                    level.registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).orElseThrow();
                net.minecraft.world.item.Item[] armorItems = {
                    net.minecraft.world.item.Items.NETHERITE_HELMET,
                    net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,
                    net.minecraft.world.item.Items.NETHERITE_LEGGINGS,
                    net.minecraft.world.item.Items.NETHERITE_BOOTS
                };
                String[] armorNames = {"Admin Helm", "Admin Chest", "Admin Legs", "Admin Boots"};
                java.util.Map<String, Integer> armorEnchs = java.util.Map.of(
                    "minecraft:protection", 10, "minecraft:unbreaking", 10, "minecraft:mending", 1,
                    "minecraft:thorns", 5, "minecraft:fire_protection", 5, "minecraft:blast_protection", 5);
                java.util.Map<String, Integer> bootEnchs = new java.util.HashMap<>(armorEnchs);
                bootEnchs.put("minecraft:feather_falling", 10);
                bootEnchs.put("minecraft:depth_strider", 3);
                for (int ai = 0; ai < armorItems.length; ai++) {
                    net.minecraft.world.item.ItemStack armor = new net.minecraft.world.item.ItemStack((net.minecraft.world.level.ItemLike) armorItems[ai]);
                    armor.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                        net.minecraft.network.chat.Component.literal("\u00a76\u00a7l" + armorNames[ai]));
                    armor.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 99999);
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable aBuilder =
                        new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    java.util.Map<String, Integer> enchMap = (ai == 3) ? bootEnchs : armorEnchs;
                    for (var entry : enchMap.entrySet()) {
                        aEnchReg.get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,
                            net.minecraft.resources.Identifier.parse(entry.getKey()))).ifPresent(ench ->
                            aBuilder.set(ench, entry.getValue()));
                    }
                    armor.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, aBuilder.toImmutable());
                    player.getInventory().add(armor);
                }
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"msg\":\"Admin Armor set given!\"}", eco);
                break;
            }
            case "admin_complete_advancements": {
                // Grant ALL advancements at once
                AdminSystem.executeCommand(player, "advancement grant @s everything");
                // Also record catalog entries in museum
                MuseumData museumData2 = MuseumData.get(level);
                for (com.ultra.megamod.feature.museum.catalog.AchievementCatalog.AchievementEntry entry : com.ultra.megamod.feature.museum.catalog.AchievementCatalog.ENTRIES) {
                    museumData2.recordAchievement(player.getUUID(), entry.advancementId());
                }
                ComputerActionHandler.sendResponse(player, "research_result", "{\"msg\":\"All advancements granted!\"}", eco);
                break;
            }
            case "admin_complete_build_orders": {
                // Instantly place all remaining blocks for all active build orders
                com.ultra.megamod.feature.schematic.data.BuildOrderManager bom =
                    com.ultra.megamod.feature.schematic.data.BuildOrderManager.get(level);
                java.util.List<com.ultra.megamod.feature.schematic.data.BuildOrder> allOrders = bom.getAllOrders();
                int completedCount = 0;
                int blocksPlaced = 0;
                for (com.ultra.megamod.feature.schematic.data.BuildOrder order : allOrders) {
                    if (order.isComplete()) continue;
                    var remaining = order.getRemainingBlocks();
                    int placed = remaining.size();
                    for (com.ultra.megamod.feature.schematic.data.BuildOrder.BuildEntry entry : remaining) {
                        try {
                            level.setBlock(entry.worldPos(), entry.state(), 3);
                        } catch (Exception ignored) {}
                    }
                    order.setProgressIndex(order.getTotalBlocks());
                    blocksPlaced += placed;
                    completedCount++;
                }
                bom.markDirty();
                bom.saveToDisk(level);
                sendResponse(player, "admin_result",
                    "{\"msg\":\"Completed " + completedCount + " build orders (" + blocksPlaced + " blocks placed)\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // ECONOMY ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_set_wallet": {
                String[] parts = jsonData.split(" ", 2);
                if (parts.length < 2) { sendResponse(player, "admin_result", "{\"msg\":\"Usage: PlayerName amount\"}", eco); break; }
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                int amount = Integer.parseInt(parts[1]);
                eco.setWallet(target.getUUID(), amount);
                eco.addAuditEntry(player.getGameProfile().name(), "admin_set_wallet", amount, "Set " + parts[0] + " wallet to " + amount);
                eco.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Set " + parts[0] + " wallet to " + amount + " MC\"}", eco);
                break;
            }
            case "admin_set_bank": {
                String[] parts = jsonData.split(" ", 2);
                if (parts.length < 2) { sendResponse(player, "admin_result", "{\"msg\":\"Usage: PlayerName amount\"}", eco); break; }
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                int amount = Integer.parseInt(parts[1]);
                eco.setBank(target.getUUID(), amount);
                eco.addAuditEntry(player.getGameProfile().name(), "admin_set_bank", amount, "Set " + parts[0] + " bank to " + amount);
                eco.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Set " + parts[0] + " bank to " + amount + " MC\"}", eco);
                break;
            }
            case "admin_give_coins_all": {
                int amount = Integer.parseInt(jsonData);
                int count = 0;
                for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                    eco.addWallet(p.getUUID(), amount);
                    count++;
                }
                eco.addAuditEntry(player.getGameProfile().name(), "admin_give_coins_all", amount, "Gave " + amount + " MC to all " + count + " players");
                eco.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Gave " + amount + " MC to " + count + " players\"}", eco);
                break;
            }
            case "admin_wipe_player_economy": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                eco.setWallet(target.getUUID(), 0);
                eco.setBank(target.getUUID(), 0);
                eco.addAuditEntry(player.getGameProfile().name(), "admin_wipe_economy", 0, "Wiped " + jsonData + " economy");
                eco.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Wiped " + jsonData + " wallet and bank\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // DUNGEON ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_reset_dungeon_cooldown": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                DungeonManager dm = DungeonManager.get(level);
                dm.unmarkAbandoned(target.getUUID());
                dm.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Reset dungeon cooldown for " + jsonData + "\"}", eco);
                break;
            }
            case "admin_give_all_dungeon_keys": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                net.minecraft.world.item.Item[] keys = {
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NORMAL.get(),
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_HARD.get(),
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NIGHTMARE.get(),
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_INFERNAL.get(),
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_MYTHIC.get(),
                    com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_ETERNAL.get()
                };
                for (net.minecraft.world.item.Item key : keys) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(key);
                    if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
                }
                sendResponse(player, "admin_result", "{\"msg\":\"Gave all 6 dungeon keys to " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            case "admin_force_complete_dungeon": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                DungeonManager dm = DungeonManager.get(level);
                DungeonManager.DungeonInstance inst = dm.getDungeonForPlayer(target.getUUID());
                if (inst == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not in a dungeon\"}", eco); break; }
                dm.completeDungeon(inst.instanceId);
                dm.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Force-completed dungeon for " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // RELIC/ACCESSORY ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_clear_accessories": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                for (AccessorySlotType slot : AccessorySlotType.values()) {
                    if (slot == AccessorySlotType.NONE) continue;
                    LibAccessoryLookup.removeEquipped(target, slot);
                }
                LibAccessoryLookup.syncToClient(target);
                sendResponse(player, "admin_result", "{\"msg\":\"Cleared all accessories for " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            case "admin_give_random_relic": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                net.minecraft.world.item.ItemStack relic = new net.minecraft.world.item.ItemStack(
                    (net.minecraft.world.level.ItemLike) net.minecraft.world.item.Items.NETHERITE_SWORD);
                WeaponStatRoller.rollAndApply(relic, 12.0f, WeaponRarity.LEGENDARY, level.random);
                if (!target.getInventory().add(relic)) target.spawnAtLocation(level, relic);
                sendResponse(player, "admin_result", "{\"msg\":\"Gave random legendary relic to " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // BACKPACK ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_give_max_backpack": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                net.minecraft.world.item.ItemStack bp = new net.minecraft.world.item.ItemStack(
                    (net.minecraft.world.level.ItemLike) com.ultra.megamod.feature.backpacks.BackpackRegistry.getItem(
                        com.ultra.megamod.feature.backpacks.BackpackVariant.NETHERITE).get());
                if (!target.getInventory().add(bp)) target.spawnAtLocation(level, bp);
                net.minecraft.world.item.Item[] upgrades = {
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.CRAFTING_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.MAGNET_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.PICKUP_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.FEEDING_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.JUKEBOX_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.REFILL_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.VOID_UPGRADE.get(),
                    com.ultra.megamod.feature.backpacks.BackpackRegistry.TANKS_UPGRADE.get()
                };
                for (net.minecraft.world.item.Item upgrade : upgrades) {
                    net.minecraft.world.item.ItemStack us = new net.minecraft.world.item.ItemStack(upgrade);
                    if (!target.getInventory().add(us)) target.spawnAtLocation(level, us);
                }
                sendResponse(player, "admin_result", "{\"msg\":\"Gave netherite backpack + all 8 upgrades to " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // PRESTIGE ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_grant_prestige": {
                String[] parts = jsonData.split(" ", 2);
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                com.ultra.megamod.feature.skills.prestige.PrestigeManager pm =
                    com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                if (parts.length > 1) {
                    try {
                        com.ultra.megamod.feature.skills.SkillTreeType tree =
                            com.ultra.megamod.feature.skills.SkillTreeType.valueOf(parts[1].toUpperCase());
                        pm.prestige(target.getUUID(), tree);
                        pm.saveToDisk(level);
                        sendResponse(player, "admin_result", "{\"msg\":\"Granted prestige in " + tree.name() + " for " + parts[0] + "\"}", eco);
                    } catch (IllegalArgumentException e) {
                        sendResponse(player, "admin_result", "{\"msg\":\"Invalid tree. Use: COMBAT, MINING, FARMING, ARCANE, SURVIVAL\"}", eco);
                    }
                } else {
                    for (com.ultra.megamod.feature.skills.SkillTreeType tree : com.ultra.megamod.feature.skills.SkillTreeType.values()) {
                        pm.prestige(target.getUUID(), tree);
                    }
                    pm.saveToDisk(level);
                    sendResponse(player, "admin_result", "{\"msg\":\"Granted prestige in ALL trees for " + parts[0] + "\"}", eco);
                }
                break;
            }
            case "admin_reset_prestige": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                com.ultra.megamod.feature.skills.prestige.PrestigeManager pm =
                    com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
                for (com.ultra.megamod.feature.skills.SkillTreeType tree : com.ultra.megamod.feature.skills.SkillTreeType.values()) {
                    while (pm.getPrestigeLevel(target.getUUID(), tree) > 0) {
                        pm.decrementPrestige(target.getUUID(), tree);
                    }
                }
                pm.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Reset all prestige for " + jsonData + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // CASINO ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_give_chips": {
                String[] parts = jsonData.split(" ", 2);
                if (parts.length < 2) { sendResponse(player, "admin_result", "{\"msg\":\"Usage: PlayerName amount\"}", eco); break; }
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                int amount = Integer.parseInt(parts[1]);
                com.ultra.megamod.feature.casino.chips.ChipManager cm =
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level);
                cm.addChipsByValue(target.getUUID(), amount);
                cm.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Gave " + amount + " chips to " + parts[0] + "\"}", eco);
                break;
            }
            case "admin_cashout_player": {
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                com.ultra.megamod.feature.casino.chips.ChipManager cm =
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level);
                int cashedOut = cm.cashOutAll(target.getUUID(), eco);
                cm.saveToDisk(level);
                eco.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Cashed out " + cashedOut + " MC for " + jsonData + "\"}", eco);
                break;
            }
            case "admin_casino_always_win": {
                String[] parts = jsonData.split(" ", 2);
                if (parts.length < 2) { sendResponse(player, "admin_result", "{\"msg\":\"Usage: PlayerName true/false\"}", eco); break; }
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(parts[0]);
                if (target == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not found\"}", eco); break; }
                boolean enabled = Boolean.parseBoolean(parts[1]);
                com.ultra.megamod.feature.casino.CasinoManager casMgr =
                    com.ultra.megamod.feature.casino.CasinoManager.get(level);
                casMgr.setAlwaysWin(target.getUUID(), enabled, level);
                casMgr.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Casino always-win " + (enabled ? "ENABLED" : "DISABLED") + " for " + parts[0] + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // CORRUPTION ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_purge_all_corruption": {
                com.ultra.megamod.feature.corruption.CorruptionManager corr =
                    com.ultra.megamod.feature.corruption.CorruptionManager.get(level);
                int zoneCount = corr.getActiveZoneCount();
                corr.clearAll();
                corr.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Purged all " + zoneCount + " corruption zones\"}", eco);
                break;
            }
            case "admin_set_corruption_at_player": {
                String[] parts = jsonData.split(" ", 2);
                int tier = parts.length > 0 ? Integer.parseInt(parts[0]) : 1;
                int radius = parts.length > 1 ? Integer.parseInt(parts[1]) : 5;
                com.ultra.megamod.feature.corruption.CorruptionManager corr =
                    com.ultra.megamod.feature.corruption.CorruptionManager.get(level);
                var zone = corr.createZone((long) player.getBlockX(), (long) player.getBlockZ(),
                    tier, "admin", level.getServer().getTickCount());
                if (zone != null) corr.setZoneRadius(zone.zoneId, radius);
                corr.saveToDisk(level);
                sendResponse(player, "admin_result", "{\"msg\":\"Created T" + tier + " corruption zone (radius " + radius + ") at your location\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // QUEST BOARD ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_refresh_quest_board": {
                long tick = level.getServer().getTickCount();
                com.ultra.megamod.feature.furniture.QuestBoardManager.forceRefresh(tick);
                int questCount = com.ultra.megamod.feature.furniture.QuestBoardManager.getQuests(tick).size();
                sendResponse(player, "admin_result", "{\"msg\":\"Quest board refreshed! " + questCount + " new quests available\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // ARENA ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_arena_skip_wave": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                com.ultra.megamod.feature.arena.ArenaManager arMgr =
                    com.ultra.megamod.feature.arena.ArenaManager.get(level);
                var arenaInst = arMgr.getInstanceForPlayer(target.getUUID());
                if (arenaInst == null) { sendResponse(player, "admin_result", "{\"msg\":\"Player not in arena\"}", eco); break; }
                arMgr.completeWave(arenaInst.instanceId, level);
                sendResponse(player, "admin_result", "{\"msg\":\"Skipped to next wave for " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // RELIQUARY APOTHECARY ADMIN (replaced the deleted MegaMod alchemy)
            // ═══════════════════════════════════════════════════════════════
            case "admin_give_all_potions": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                net.minecraft.world.item.Item[] potions = {
                    com.ultra.megamod.reliquary.init.ModItems.POTION.get(),
                    com.ultra.megamod.reliquary.init.ModItems.LINGERING_POTION.get(),
                    com.ultra.megamod.reliquary.init.ModItems.SPLASH_POTION.get(),
                    com.ultra.megamod.reliquary.init.ModItems.APHRODITE_POTION.get(),
                    com.ultra.megamod.reliquary.init.ModItems.FERTILE_POTION.get(),
                    com.ultra.megamod.reliquary.init.ModItems.ANGELHEART_VIAL.get(),
                    com.ultra.megamod.reliquary.init.ModItems.GLOWING_WATER.get()
                };
                int given = 0;
                for (net.minecraft.world.item.Item potion : potions) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(potion, 3);
                    if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
                    given++;
                }
                sendResponse(player, "admin_result", "{\"msg\":\"Gave " + given + " Reliquary potion types (x3 each) to " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            case "admin_give_all_reagents": {
                ServerPlayer target = jsonData.isEmpty() ? player : level.getServer().getPlayerList().getPlayerByName(jsonData);
                if (target == null) target = player;
                // Reliquary's reagents are the potion-ingredient-bearing mob drops
                net.minecraft.world.item.Item[] reagents = {
                    com.ultra.megamod.reliquary.init.ModItems.POTION_ESSENCE.get(),
                    com.ultra.megamod.reliquary.init.ModItems.SLIME_PEARL.get(),
                    com.ultra.megamod.reliquary.init.ModItems.CATALYZING_GLAND.get(),
                    com.ultra.megamod.reliquary.init.ModItems.CHELICERAE.get(),
                    com.ultra.megamod.reliquary.init.ModItems.RIB_BONE.get(),
                    com.ultra.megamod.reliquary.init.ModItems.MOLTEN_CORE.get(),
                    com.ultra.megamod.reliquary.init.ModItems.BAT_WING.get(),
                    com.ultra.megamod.reliquary.init.ModItems.EYE_OF_THE_STORM.get(),
                    com.ultra.megamod.reliquary.init.ModItems.FROZEN_CORE.get(),
                    com.ultra.megamod.reliquary.init.ModItems.NEBULOUS_HEART.get()
                };
                for (net.minecraft.world.item.Item reagent : reagents) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(reagent, 16);
                    if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
                }
                sendResponse(player, "admin_result", "{\"msg\":\"Gave all 10 Reliquary reagent types (x16 each) to " + target.getGameProfile().name() + "\"}", eco);
                break;
            }
            // ═══════════════════════════════════════════════════════════════
            // BOUNTY ADMIN
            // ═══════════════════════════════════════════════════════════════
            case "admin_create_bounty": {
                String[] parts = jsonData.split(" ", 3);
                if (parts.length < 3) { sendResponse(player, "admin_result", "{\"msg\":\"Usage: itemId quantity reward\"}", eco); break; }
                String itemId = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                int reward = Integer.parseInt(parts[2]);
                String bountyJson = "{\"itemId\":\"" + itemId + "\",\"quantity\":" + quantity + ",\"price\":" + reward + ",\"system\":true}";
                com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.handle(player, "bounty_post", bountyJson, level, eco);
                sendResponse(player, "admin_result", "{\"msg\":\"Created bounty: " + quantity + "x " + itemId + " for " + reward + " MC\"}", eco);
                break;
            }
            case "admin_clear_all_bounties": {
                com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.adminEnsureLoaded(level);
                var allBounties = com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.getAllBounties();
                int removed = 0;
                for (int i = allBounties.size() - 1; i >= 0; i--) {
                    com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.adminRemoveBounty(allBounties.get(i).id, level, eco);
                    removed++;
                }
                sendResponse(player, "admin_result", "{\"msg\":\"Cleared " + removed + " bounties\"}", eco);
                break;
            }

            case "request_performance": {
                Runtime rt = Runtime.getRuntime();
                long maxMem = rt.maxMemory() / 1024 / 1024;
                long usedMem = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                long totalMem = rt.totalMemory() / 1024 / 1024;
                int entityCount = 0;
                int loadedChunks = 0;
                for (ServerLevel dim : level.getServer().getAllLevels()) {
                    entityCount += dim.getAllEntities().spliterator().estimateSize() > 0 ? (int) java.util.stream.StreamSupport.stream(dim.getAllEntities().spliterator(), false).count() : 0;
                    try { loadedChunks += dim.getChunkSource().getLoadedChunksCount(); } catch (Exception ignored) {}
                }
                int playerCount = level.getServer().getPlayerCount();
                double avgTickMs = 0;
                try {
                    // Access tickTimes field via reflection (may be private in 1.21.11)
                    java.lang.reflect.Field f = level.getServer().getClass().getSuperclass().getDeclaredField("tickTimes");
                    f.setAccessible(true);
                    long[] tickTimesArr = (long[]) f.get(level.getServer());
                    if (tickTimesArr != null && tickTimesArr.length > 0) {
                        long sum = 0;
                        for (long t : tickTimesArr) sum += t;
                        avgTickMs = (sum / (double) tickTimesArr.length) / 1_000_000.0;
                    }
                } catch (Exception ignored) {
                    try {
                        // Fallback: try getAverageTickTimeNanos
                        long avg = level.getServer().getAverageTickTimeNanos();
                        avgTickMs = avg / 1_000_000.0;
                    } catch (Exception ignored2) {}
                }
                double tps = Math.min(20.0, avgTickMs > 0 ? 1000.0 / avgTickMs : 20.0);
                // System stats for dashboard
                int activeArenas = 0;
                try { activeArenas = com.ultra.megamod.feature.arena.ArenaManager.get(level).getActiveArenaCount(); } catch (Exception ignored) {}
                int activeBounties = 0;
                try { activeBounties = com.ultra.megamod.feature.bountyhunt.BountyHuntManager.getAvailableBounties().size(); } catch (Exception ignored) {}
                int activeDungeons = 0;
                try { activeDungeons = DungeonManager.get(level).getActiveInstanceCount(); } catch (Exception ignored) {}
                // Record to TPS history
                var history = com.ultra.megamod.feature.computer.network.handlers.SystemHealthHandler.getHistory();
                StringBuilder histSb = new StringBuilder("[");
                for (int hi = 0; hi < history.size(); hi++) {
                    var h = history.get(hi);
                    if (hi > 0) histSb.append(",");
                    histSb.append(String.format("{\"tps\":%.1f,\"mem\":%d}", h.tps(), h.usedMemMb() * 100 / Math.max(1, h.maxMemMb())));
                }
                histSb.append("]");
                // Build issues list
                StringBuilder issues = new StringBuilder("[");
                boolean issFirst = true;
                if (tps < 15) { issues.append("{\"level\":\"ERROR\",\"msg\":\"TPS critically low: ").append(String.format("%.1f", tps)).append("\"}"); issFirst = false; }
                else if (tps < 18) { issues.append("{\"level\":\"WARN\",\"msg\":\"TPS below normal: ").append(String.format("%.1f", tps)).append("\"}"); issFirst = false; }
                if (maxMem > 0 && usedMem * 100 / maxMem > 85) { if (!issFirst) issues.append(","); issues.append("{\"level\":\"WARN\",\"msg\":\"Memory usage high: ").append(usedMem * 100 / maxMem).append("%\"}"); issFirst = false; }
                if (entityCount > 5000) { if (!issFirst) issues.append(","); issues.append("{\"level\":\"WARN\",\"msg\":\"High entity count: ").append(entityCount).append("\"}"); }
                issues.append("]");
                String perfJson = String.format("{\"tps\":%.1f,\"mspt\":%.1f,\"mem\":%d,\"maxMem\":%d,\"totalMem\":%d,\"entities\":%d,\"chunks\":%d,\"players\":%d,\"activeArenas\":%d,\"activeBounties\":%d,\"activeDungeons\":%d,\"tpsHistory\":%s,\"issues\":%s}",
                    tps, avgTickMs, usedMem, maxMem, totalMem, entityCount, loadedChunks, playerCount, activeArenas, activeBounties, activeDungeons, histSb, issues);
                ComputerActionHandler.sendResponse(player, "performance_data", perfJson, eco);
                break;
            }
            case "request_entities": {
                AABB scanBox = new AABB(player.getX() - 50, player.getY() - 50, player.getZ() - 50,
                    player.getX() + 50, player.getY() + 50, player.getZ() + 50);
                List<Entity> nearby = level.getEntities(player, scanBox);
                nearby.sort((a, b) -> Double.compare(a.distanceTo(player), b.distanceTo(player)));
                StringBuilder esb = new StringBuilder("[");
                boolean eFirst = true;
                int eCount = 0;
                for (Entity e : nearby) {
                    if (eCount >= 50) break;
                    if (!eFirst) esb.append(",");
                    eFirst = false;
                    String etype = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                    String ename = e.getName().getString().replace("\"", "\\\"");
                    float hp = 0, maxHp = 0;
                    if (e instanceof LivingEntity le) {
                        hp = le.getHealth();
                        maxHp = le.getMaxHealth();
                    }
                    esb.append(String.format("{\"id\":%d,\"type\":\"%s\",\"name\":\"%s\",\"x\":%.1f,\"y\":%.1f,\"z\":%.1f,\"health\":%.1f,\"maxHealth\":%.1f,\"distance\":%.1f}",
                        e.getId(), etype, ename, e.getX(), e.getY(), e.getZ(), hp, maxHp, e.distanceTo(player)));
                    eCount++;
                }
                esb.append("]");
                ComputerActionHandler.sendResponse(player, "entity_data", esb.toString(), eco);
                break;
            }
            case "admin_kill_hostile": {
                if (!AdminSystem.isAdmin(player)) break;
                AABB hostileBox = new AABB(player.getX() - 50, player.getY() - 50, player.getZ() - 50,
                    player.getX() + 50, player.getY() + 50, player.getZ() + 50);
                List<Entity> hostileNearby = level.getEntities(player, hostileBox);
                int killCount = 0;
                for (Entity e : hostileNearby) {
                    if (e instanceof Monster) {
                        e.kill(level);
                        killCount++;
                    }
                }
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Killed " + killCount + " hostile mobs\"}", eco);
                break;
            }
            case "admin_kill_entity": {
                try {
                    int eid = Integer.parseInt(jsonData);
                    Entity target = level.getEntity(eid);
                    if (target != null && !(target instanceof ServerPlayer)) {
                        target.kill(level);
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Killed " + target.getName().getString().replace("\"", "\\\"") + "\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Entity not found or is a player\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Invalid entity ID\"}", eco);
                }
                break;
            }
            case "admin_tp_to_entity": {
                try {
                    int eid = Integer.parseInt(jsonData);
                    Entity target = level.getEntity(eid);
                    if (target != null) {
                        player.teleportTo(level, target.getX(), target.getY(), target.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Teleported to " + target.getName().getString().replace("\"", "\\\"") + "\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Entity not found\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Invalid entity ID\"}", eco);
                }
                break;
            }
            case "request_server_logs": {
                StringBuilder logSb = new StringBuilder("[");
                try {
                    java.io.File logFile = new java.io.File("logs/latest.log");
                    if (logFile.exists()) {
                        java.util.List<String> allLines = java.nio.file.Files.readAllLines(logFile.toPath());
                        int start = Math.max(0, allLines.size() - 50);
                        boolean first = true;
                        for (int i = start; i < allLines.size(); i++) {
                            if (!first) logSb.append(",");
                            first = false;
                            String escaped = allLines.get(i).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "");
                            if (escaped.length() > 200) escaped = escaped.substring(0, 200) + "...";
                            logSb.append("\"").append(escaped).append("\"");
                        }
                    }
                } catch (Exception e) {
                    logSb.append("\"Error reading logs: ").append(e.getMessage()).append("\"");
                }
                logSb.append("]");
                ComputerActionHandler.sendResponse(player, "log_data", logSb.toString(), eco);
                break;
            }
            case "request_suggestions": {
                String partial = jsonData;
                // Strip leading / if present (terminal doesn't need it)
                if (partial.startsWith("/")) partial = partial.substring(1);
                try {
                    com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher =
                        level.getServer().getCommands().getDispatcher();
                    // Use elevated permission source so admin commands show up
                    net.minecraft.commands.CommandSourceStack source = player.createCommandSourceStack()
                        .withPermission(net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS);

                    java.util.LinkedHashSet<String> allSuggestions = new java.util.LinkedHashSet<>();

                    // Get suggestions for current text (completes the current token)
                    com.mojang.brigadier.ParseResults<net.minecraft.commands.CommandSourceStack> parse =
                        dispatcher.parse(partial, source);
                    com.mojang.brigadier.suggestion.Suggestions suggestions =
                        dispatcher.getCompletionSuggestions(parse).join();
                    for (com.mojang.brigadier.suggestion.Suggestion s : suggestions.getList()) {
                        allSuggestions.add(s.getText());
                    }

                    // Also get next-argument suggestions (e.g. "xp" -> show "add", "set", "query")
                    if (!partial.isEmpty() && !partial.endsWith(" ")) {
                        com.mojang.brigadier.ParseResults<net.minecraft.commands.CommandSourceStack> parseNext =
                            dispatcher.parse(partial + " ", source);
                        com.mojang.brigadier.suggestion.Suggestions nextSuggestions =
                            dispatcher.getCompletionSuggestions(parseNext).join();
                        for (com.mojang.brigadier.suggestion.Suggestion s : nextSuggestions.getList()) {
                            allSuggestions.add(s.getText());
                        }
                    }

                    StringBuilder sb = new StringBuilder("[");
                    boolean first = true;
                    int count = 0;
                    for (String s : allSuggestions) {
                        if (count >= 15) break;
                        if (!first) sb.append(",");
                        first = false;
                        sb.append("\"").append(s.replace("\"", "\\\"")).append("\"");
                        count++;
                    }
                    sb.append("]");
                    PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new ComputerDataPayload("suggestions_data", sb.toString(), 0, 0), (CustomPacketPayload[])new CustomPacketPayload[0]);
                } catch (Exception e) {
                    PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new ComputerDataPayload("suggestions_data", "[]", 0, 0), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
                break;
            }
            // === New System Admin Commands (all require admin) ===
            case "admin_grant_mastery_marks": {
                if (!AdminSystem.isAdmin(player)) break;
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 2) return;
                    UUID targetId = UUID.fromString(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    com.ultra.megamod.feature.prestige.MasteryMarkManager.get(level).addMarks(targetId, amount);
                    com.ultra.megamod.feature.prestige.MasteryMarkManager.get(level).saveToDisk(level);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Granted " + amount + " Mastery Marks\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_reset_arena_elo": {
                if (!AdminSystem.isAdmin(player)) break;
                try {
                    UUID targetId = UUID.fromString(jsonData);
                    com.ultra.megamod.feature.arena.ArenaManager.get(level).getOrCreateStats(targetId).eloRating = 1000;
                    com.ultra.megamod.feature.arena.ArenaManager.get(level).getOrCreateStats(targetId).pvpWins = 0;
                    com.ultra.megamod.feature.arena.ArenaManager.get(level).getOrCreateStats(targetId).pvpLosses = 0;
                    com.ultra.megamod.feature.arena.ArenaManager.get(level).saveToDisk(level);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Arena ELO reset to 1000\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_grant_ngplus_tier": {
                if (!AdminSystem.isAdmin(player)) break;
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 2) return;
                    UUID targetId = UUID.fromString(parts[0]);
                    String tierName = parts[1]; // e.g. "INFERNAL" or "MYTHIC"
                    com.ultra.megamod.feature.dungeons.NewGamePlusManager ngp = com.ultra.megamod.feature.dungeons.NewGamePlusManager.get(level);
                    // Grant all 8 boss defeats for that tier
                    for (String bossId : java.util.List.of("wraith", "ossukage", "dungeon_keeper", "frostmaw",
                            "wroughtnaut", "umvuthi", "chaos_spawner", "sculptor")) {
                        ngp.recordBossDefeat(targetId, bossId, tierName);
                    }
                    ngp.saveToDisk(level);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Granted all " + tierName + " boss defeats\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_reset_challenges": {
                if (!AdminSystem.isAdmin(player)) break;
                try {
                    UUID targetId = UUID.fromString(jsonData);
                    // Reset challenge progress for this player (clear their progress array)
                    ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetId);
                    if (targetPlayer != null) {
                        int[] progress = com.ultra.megamod.feature.skills.challenges.SkillChallenges.getPlayerProgress(targetId, level);
                        java.util.Arrays.fill(progress, 0);
                        ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"success\":true,\"msg\":\"Challenge progress reset\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"success\":false,\"msg\":\"Player not found\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}", eco);
                }
                break;
            }
            case "admin_force_end_arena": {
                if (!AdminSystem.isAdmin(player)) break;
                try {
                    com.ultra.megamod.feature.arena.ArenaManager arena = com.ultra.megamod.feature.arena.ArenaManager.get(level);
                    UUID targetId = UUID.fromString(jsonData);
                    com.ultra.megamod.feature.arena.ArenaManager.ArenaInstance inst = arena.getInstanceForPlayer(targetId);
                    if (inst != null) {
                        arena.endArena(inst.instanceId, false, level);
                        ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"success\":true,\"msg\":\"Arena force-ended\"}", eco);
                    } else {
                        ComputerActionHandler.sendResponse(player, "admin_result",
                            "{\"success\":false,\"msg\":\"Player not in arena\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}", eco);
                }
                break;
            }

            // ==================== VANISH MODE ====================
            case "vanish_toggle": {
                com.ultra.megamod.feature.computer.admin.VanishManager.toggle(player);
                boolean vanished = com.ultra.megamod.feature.computer.admin.VanishManager.isVanished(player.getUUID());
                ComputerActionHandler.sendResponse(player, "admin_result",
                    "{\"success\":true,\"msg\":\"Vanish " + (vanished ? "ON" : "OFF") + "\"}", eco);
                break;
            }

            // ==================== REAL-TIME PLAYER TRACKER ====================
            case "admin_player_tracker": {
                StringBuilder sb = new StringBuilder("{\"players\":[");
                boolean first = true;
                for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"name\":\"").append(p.getGameProfile().name().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"uuid\":\"").append(p.getUUID()).append("\"");
                    sb.append(",\"x\":").append((int) p.getX());
                    sb.append(",\"y\":").append((int) p.getY());
                    sb.append(",\"z\":").append((int) p.getZ());
                    sb.append(",\"dim\":\"").append(p.level().dimension().identifier().toString()).append("\"");
                    sb.append(",\"health\":").append(Math.round(p.getHealth()));
                    sb.append(",\"maxHealth\":").append(Math.round(p.getMaxHealth()));
                    sb.append(",\"food\":").append(p.getFoodData().getFoodLevel());
                    sb.append(",\"gamemode\":\"").append(p.gameMode.getGameModeForPlayer().getName()).append("\"");
                    sb.append(",\"vanished\":").append(com.ultra.megamod.feature.computer.admin.VanishManager.isVanished(p.getUUID()));
                    sb.append("}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "player_tracker_data", sb.toString(), eco);
                break;
            }

            // ==================== DEATH LOG VIEWER ====================
            case "deathlog_request": {
                var deathMgr = com.ultra.megamod.feature.computer.admin.DeathLogManager.get(level);
                var recent = deathMgr.getRecentEntries(50);
                StringBuilder sb = new StringBuilder("{\"deaths\":[");
                boolean first = true;
                for (int di = recent.size() - 1; di >= 0; di--) {
                    var d = recent.get(di);
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"name\":\"").append(d.playerName().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"x\":").append((int) d.x());
                    sb.append(",\"y\":").append((int) d.y());
                    sb.append(",\"z\":").append((int) d.z());
                    sb.append(",\"dim\":\"").append(d.dimension()).append("\"");
                    sb.append(",\"cause\":\"").append(d.cause().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"time\":").append(d.timestamp());
                    sb.append(",\"itemCount\":").append(d.items().size());
                    sb.append(",\"items\":[");
                    boolean fi = true;
                    for (var item : d.items()) {
                        if (!fi) sb.append(",");
                        fi = false;
                        sb.append("{\"name\":\"").append(item.itemName().replace("\"", "\\\"")).append("\"");
                        sb.append(",\"count\":").append(item.count()).append("}");
                    }
                    sb.append("]}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "deathlog_data", sb.toString(), eco);
                break;
            }
            case "deathlog_tp": {
                // Format: x:y:z:dimension
                try {
                    String[] parts = jsonData.split(":");
                    if (parts.length < 3) return;
                    int dx = Integer.parseInt(parts[0]);
                    int dy = Integer.parseInt(parts[1]);
                    int dz = Integer.parseInt(parts[2]);
                    String result = AdminSystem.executeCommand(player, "tp @s " + dx + " " + dy + " " + dz);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Teleported to death location\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"TP error\"}", eco);
                }
                break;
            }

            // ==================== ENTITY CLEANUP ====================
            case "cleanup_by_type": {
                // Format: entityType (e.g. "minecraft:zombie")
                try {
                    String cmd = "kill @e[type=" + jsonData.trim() + "]";
                    String result = AdminSystem.executeCommand(player, cmd);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"" + result.replace("\"", "\\\"").replace("\n", " ") + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"Cleanup error\"}", eco);
                }
                break;
            }
            case "cleanup_radius": {
                // Format: radius:type (e.g. "50:minecraft:zombie" or "50:all")
                try {
                    String[] parts = jsonData.split(":", 2);
                    int radius = Integer.parseInt(parts[0]);
                    String type = parts.length > 1 ? parts[1].trim() : "all";
                    String typeFilter = "all".equals(type) ? "type=!player" : "type=" + type;
                    String cmd = "kill @e[" + typeFilter + ",distance=.." + radius + "]";
                    String result = AdminSystem.executeCommand(player, cmd);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"" + result.replace("\"", "\\\"").replace("\n", " ") + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"Cleanup error\"}", eco);
                }
                break;
            }
            case "cleanup_dimension": {
                // Kills all non-player entities in current dimension
                try {
                    String dimKey = player.level().dimension().identifier().toString();
                    int killed = 0;
                    for (Entity e : ((ServerLevel) player.level()).getAllEntities()) {
                        if (e instanceof ServerPlayer) continue;
                        if (e instanceof LivingEntity) {
                            e.discard();
                            killed++;
                        }
                    }
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Removed " + killed + " entities in " + dimKey + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"Cleanup error\"}", eco);
                }
                break;
            }
            case "cleanup_items": {
                // Kill all item entities on ground
                try {
                    String result = AdminSystem.executeCommand(player, "kill @e[type=item]");
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"" + result.replace("\"", "\\\"").replace("\n", " ") + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"Cleanup error\"}", eco);
                }
                break;
            }

            // ==================== COMMAND ALIASES ====================
            case "alias_request": {
                var aliasMgr = com.ultra.megamod.feature.computer.admin.CommandAliasManager.get(level);
                StringBuilder sb = new StringBuilder("{\"aliases\":[");
                boolean first = true;
                for (var entry : aliasMgr.getAliases().entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"name\":\"").append(entry.getKey().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"cmd\":\"").append(entry.getValue().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "alias_data", sb.toString(), eco);
                break;
            }
            case "alias_add": {
                // Format: name:command
                int colonIdx = jsonData.indexOf(':');
                if (colonIdx < 0) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Format: name:command\"}", eco);
                    break;
                }
                String aliasName = jsonData.substring(0, colonIdx).trim();
                String aliasCmd = jsonData.substring(colonIdx + 1).trim();
                var aliasMgr = com.ultra.megamod.feature.computer.admin.CommandAliasManager.get(level);
                String err = aliasMgr.addAlias(aliasName, aliasCmd);
                if (err != null) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"" + err + "\"}", eco);
                } else {
                    aliasMgr.saveToDisk(level);
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Alias '" + aliasName + "' saved\"}", eco);
                }
                break;
            }
            case "alias_remove": {
                var aliasMgr = com.ultra.megamod.feature.computer.admin.CommandAliasManager.get(level);
                boolean removed = aliasMgr.removeAlias(jsonData.trim());
                aliasMgr.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result",
                    "{\"success\":" + removed + ",\"msg\":\"" + (removed ? "Alias removed" : "Alias not found") + "\"}", eco);
                break;
            }
            case "alias_execute": {
                var aliasMgr = com.ultra.megamod.feature.computer.admin.CommandAliasManager.get(level);
                String resolved = aliasMgr.resolve(jsonData.trim(), player.getGameProfile().name());
                if (resolved == null) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Unknown alias\"}", eco);
                } else {
                    String result = AdminSystem.executeCommand(player, resolved);
                    String escaped = result.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
                    ComputerActionHandler.sendResponse(player, "command_result", "{\"success\":true,\"output\":\"" + escaped + "\"}", eco);
                }
                break;
            }

            // ==================== CUSTOM LOOT TABLES ====================
            case "loot_request": {
                var lootMgr = com.ultra.megamod.feature.computer.admin.AdminLootManager.get(level);
                StringBuilder sb = new StringBuilder("{\"mobs\":[");
                boolean first = true;
                for (var entry : lootMgr.getAllDrops().entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"mobId\":\"").append(entry.getKey().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"drops\":[");
                    boolean fd = true;
                    for (var drop : entry.getValue()) {
                        if (!fd) sb.append(",");
                        fd = false;
                        sb.append("{\"item\":\"").append(drop.itemId().replace("\"", "\\\"")).append("\"");
                        sb.append(",\"min\":").append(drop.minCount());
                        sb.append(",\"max\":").append(drop.maxCount());
                        sb.append(",\"chance\":").append(drop.chance()).append("}");
                    }
                    sb.append("]}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "loot_data", sb.toString(), eco);
                break;
            }
            case "loot_add": {
                // Format: mobId|itemId|min|max|chance
                try {
                    String[] parts = jsonData.split("\\|");
                    if (parts.length < 5) return;
                    String mobId = parts[0].trim();
                    String itemId = parts[1].trim();
                    int min = Integer.parseInt(parts[2].trim());
                    int max = Integer.parseInt(parts[3].trim());
                    double chance = Double.parseDouble(parts[4].trim());
                    var lootMgr = com.ultra.megamod.feature.computer.admin.AdminLootManager.get(level);
                    String err = lootMgr.addDrop(mobId, itemId, min, max, chance);
                    if (err != null) {
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"" + err + "\"}", eco);
                    } else {
                        lootMgr.saveToDisk(level);
                        ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"Loot drop added\"}", eco);
                    }
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Invalid format\"}", eco);
                }
                break;
            }
            case "loot_remove": {
                // Format: mobId|index
                try {
                    String[] parts = jsonData.split("\\|");
                    if (parts.length < 2) return;
                    var lootMgr = com.ultra.megamod.feature.computer.admin.AdminLootManager.get(level);
                    boolean removed = lootMgr.removeDrop(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    lootMgr.saveToDisk(level);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":" + removed + ",\"msg\":\"" + (removed ? "Drop removed" : "Not found") + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Invalid format\"}", eco);
                }
                break;
            }
            case "loot_clear_mob": {
                var lootMgr = com.ultra.megamod.feature.computer.admin.AdminLootManager.get(level);
                lootMgr.clearDropsForMob(jsonData.trim());
                lootMgr.saveToDisk(level);
                ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":true,\"msg\":\"All drops cleared for mob\"}", eco);
                break;
            }

            // ==================== UNDO/ROLLBACK ====================
            case "undo_request": {
                var recent = com.ultra.megamod.feature.computer.admin.AdminUndoManager.getRecent(20);
                StringBuilder sb = new StringBuilder("{\"history\":[");
                boolean first = true;
                for (int ui = recent.size() - 1; ui >= 0; ui--) {
                    var u = recent.get(ui);
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"type\":\"").append(u.type()).append("\"");
                    sb.append(",\"admin\":\"").append(u.adminName().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"target\":\"").append(u.targetName().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"desc\":\"").append(u.description().replace("\"", "\\\"")).append("\"");
                    sb.append(",\"time\":").append(u.timestamp()).append("}");
                }
                sb.append("]}");
                ComputerActionHandler.sendResponse(player, "undo_data", sb.toString(), eco);
                break;
            }
            case "undo_last": {
                var entry = com.ultra.megamod.feature.computer.admin.AdminUndoManager.popLast();
                if (entry == null) {
                    ComputerActionHandler.sendResponse(player, "admin_result", "{\"success\":false,\"msg\":\"Nothing to undo\"}", eco);
                } else {
                    String result = AdminSystem.executeCommand(player, entry.undoCommand());
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Undone: " + entry.description().replace("\"", "\\\"") + "\"}", eco);
                }
                break;
            }
            case "set_combat_config": {
                if (!AdminSystem.isAdmin(player)) break;
                // JSON: {"key":"passive_proc_multiplier","value":1.5} or {"key":"allow_fast_attacks","value":true}
                try {
                    String cfgKey = extractJsonString(jsonData, "key");
                    String rawValue = extractJsonValue(jsonData, "value");
                    if (cfgKey == null || rawValue == null) break;
                    Object parsed;
                    if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
                        parsed = Boolean.parseBoolean(rawValue);
                    } else {
                        parsed = Double.parseDouble(rawValue);
                    }
                    com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.setAndSave(cfgKey, parsed);
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":true,\"msg\":\"Combat " + cfgKey + " = " + rawValue + "\"}", eco);
                } catch (Exception e) {
                    ComputerActionHandler.sendResponse(player, "admin_result",
                        "{\"success\":false,\"msg\":\"Bad config payload: " + e.getMessage().replace("\"", "'") + "\"}", eco);
                }
                break;
            }
        }
    }

    /** Tiny JSON extractor for {"key":"<str>","value":"<str|num|bool>"}. Returns null if missing. */
    private static String extractJsonString(String json, String field) {
        if (json == null) return null;
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int quote = json.indexOf('"', colon);
        if (quote < 0) return null;
        int end = json.indexOf('"', quote + 1);
        if (end < 0) return null;
        return json.substring(quote + 1, end);
    }

    /** Extracts a value — either quoted string or raw number/bool — following the given field. */
    private static String extractJsonValue(String json, String field) {
        if (json == null) return null;
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int pos = colon + 1;
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        if (pos >= json.length()) return null;
        char c = json.charAt(pos);
        if (c == '"') {
            int end = json.indexOf('"', pos + 1);
            return end < 0 ? null : json.substring(pos + 1, end);
        }
        int end = pos;
        while (end < json.length() && "-0123456789.eEtrufals".indexOf(json.charAt(end)) >= 0) end++;
        return json.substring(pos, end);
    }

    private static void sendEconomyData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        StringBuilder sb = new StringBuilder("{\"players\":[");
        Map<UUID, int[]> all = eco.getAllPlayerData();
        boolean first = true;
        for (Map.Entry<UUID, int[]> entry : all.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            UUID uuid = entry.getKey();
            String name = ComputerActionHandler.resolveName(uuid, level);
            sb.append("{\"uuid\":\"").append(uuid)
              .append("\",\"name\":\"").append(name.replace("\"", "\\\""))
              .append("\",\"wallet\":").append(entry.getValue()[0])
              .append(",\"bank\":").append(entry.getValue()[1]).append("}");
        }
        sb.append("],\"totalWallets\":").append(eco.getTotalWallets())
          .append(",\"totalBanks\":").append(eco.getTotalBanks())
          .append(",\"playerCount\":").append(eco.getPlayerCount()).append("}");
        ComputerActionHandler.sendResponse(player, "economy_data", sb.toString(), eco);
    }

    private static void sendSkillData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        SkillManager skills = SkillManager.get(level);
        StringBuilder sb = new StringBuilder("{\"players\":[");
        Map<UUID, SkillManager.PlayerSkillData> all = skills.getAllPlayerData();
        boolean first = true;
        for (Map.Entry<UUID, SkillManager.PlayerSkillData> entry : all.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            UUID uuid = entry.getKey();
            SkillManager.PlayerSkillData data = entry.getValue();
            String name = ComputerActionHandler.resolveName(uuid, level);
            sb.append("{\"uuid\":\"").append(uuid)
              .append("\",\"name\":\"").append(name.replace("\"", "\\\""))
              .append("\",\"points\":").append(data.getAvailablePoints());
            sb.append(",\"treePoints\":{");
            boolean firstTree = true;
            for (SkillTreeType tree : SkillTreeType.values()) {
                if (!firstTree) sb.append(",");
                firstTree = false;
                sb.append("\"").append(tree.name()).append("\":").append(data.getAvailablePoints(tree));
            }
            sb.append("}");
            for (SkillTreeType tree : SkillTreeType.values()) {
                sb.append(",\"").append(tree.name()).append("\":[")
                  .append(data.getLevel(tree)).append(",").append(data.getXp(tree)).append("]");
            }
            sb.append("}");
        }
        sb.append("],\"adminXpMult\":").append(String.format("%.1f", skills.getAdminXpMultiplier()))
          .append(",\"adminOnlyXpBoost\":").append(String.format("%.1f", skills.getAdminOnlyXpBoost())).append("}");
        ComputerActionHandler.sendResponse(player, "skills_data", sb.toString(), eco);
    }

    private static void sendCosmeticData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        SkillManager skills = SkillManager.get(level);
        com.ultra.megamod.feature.skills.prestige.PrestigeManager prestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager.get(level);
        com.ultra.megamod.feature.computer.network.handlers.SettingsHandler settings = null; // static methods
        Map<UUID, SkillManager.PlayerSkillData> all = skills.getAllPlayerData();
        StringBuilder sb = new StringBuilder("{\"players\":[");
        boolean first = true;
        for (Map.Entry<UUID, SkillManager.PlayerSkillData> entry : all.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            UUID uuid = entry.getKey();
            String name = ComputerActionHandler.resolveName(uuid, level);
            // Get badge info
            com.ultra.megamod.feature.skills.SkillBadges.BadgeInfo badge = com.ultra.megamod.feature.skills.SkillBadges.getBadge(uuid, level);
            boolean badgeEnabled = com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.isEnabled(uuid, "skill_badge");
            boolean particlesEnabled = com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.isEnabled(uuid, "skill_particles");
            int totalPrestige = prestige.getTotalPrestige(uuid);
            // Count total respecs across all trees
            int totalRespec = 0;
            for (SkillTreeType t : SkillTreeType.values()) {
                totalRespec += skills.getRespecCount(uuid, t);
            }
            sb.append("{\"uuid\":\"").append(uuid)
              .append("\",\"name\":\"").append(name.replace("\"", "\\\"")).append("\"");
            if (badge != null) {
                sb.append(",\"badgeTitle\":\"").append(badge.title().replace("\"", "\\\"")).append("\"");
                sb.append(",\"badgeTier\":").append(badge.tier());
                sb.append(",\"badgeTree\":\"").append(badge.tree().name()).append("\"");
            } else {
                sb.append(",\"badgeTitle\":\"\",\"badgeTier\":0,\"badgeTree\":\"\"");
            }
            sb.append(",\"badgeEnabled\":").append(badgeEnabled);
            sb.append(",\"particlesEnabled\":").append(particlesEnabled);
            sb.append(",\"totalPrestige\":").append(totalPrestige);
            sb.append(",\"respecCount\":").append(totalRespec);
            boolean hasCustom = com.ultra.megamod.feature.skills.SkillBadges.hasCustomBadge(uuid);
            sb.append(",\"hasCustomBadge\":").append(hasCustom);
            if (hasCustom) {
                String ct = com.ultra.megamod.feature.skills.SkillBadges.getCustomTitle(uuid);
                String cc = com.ultra.megamod.feature.skills.SkillBadges.getCustomColor(uuid);
                sb.append(",\"customTitle\":\"").append(ct != null ? ct.replace("\"", "\\\"") : "").append("\"");
                sb.append(",\"customColor\":\"").append(cc != null ? cc : "white").append("\"");
            }
            sb.append(",\"treePrestige\":{");
            boolean firstTree = true;
            for (SkillTreeType t : SkillTreeType.values()) {
                if (!firstTree) sb.append(",");
                firstTree = false;
                sb.append("\"").append(t.name()).append("\":").append(prestige.getPrestigeLevel(uuid, t));
            }
            sb.append("}}");
        }
        sb.append("]}");
        ComputerActionHandler.sendResponse(player, "cosmetics_data", sb.toString(), eco);
    }

    private static void sendPartyViewData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        Map<UUID, com.ultra.megamod.feature.computer.network.handlers.PartyHandler.Party> allParties =
            com.ultra.megamod.feature.computer.network.handlers.PartyHandler.getAllParties();
        SkillManager skills = SkillManager.get(level);
        StringBuilder sb = new StringBuilder("{\"parties\":[");
        boolean first = true;
        for (Map.Entry<UUID, com.ultra.megamod.feature.computer.network.handlers.PartyHandler.Party> entry : allParties.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            com.ultra.megamod.feature.computer.network.handlers.PartyHandler.Party party = entry.getValue();
            String leaderName = ComputerActionHandler.resolveName(party.leader(), level);
            sb.append("{\"leaderName\":\"").append(leaderName.replace("\"", "\\\"")).append("\"");
            sb.append(",\"leaderUuid\":\"").append(party.leader()).append("\"");
            // Members
            sb.append(",\"members\":[");
            boolean firstMem = true;
            for (UUID memberId : party.members()) {
                if (!firstMem) sb.append(",");
                firstMem = false;
                String memName = ComputerActionHandler.resolveName(memberId, level);
                boolean online = level.getServer().getPlayerList().getPlayer(memberId) != null;
                sb.append("{\"name\":\"").append(memName.replace("\"", "\\\"")).append("\",\"online\":").append(online).append("}");
            }
            sb.append("]");
            // Check combo buff
            java.util.Set<com.ultra.megamod.feature.skills.SkillBranch> partySpecs = new java.util.HashSet<>();
            for (UUID memberId : party.members()) {
                java.util.Set<String> nodes = skills.getUnlockedNodes(memberId);
                for (com.ultra.megamod.feature.skills.SkillBranch branch : com.ultra.megamod.feature.skills.SkillBranch.values()) {
                    if (nodes.contains(branch.name().toLowerCase() + "_3")) {
                        partySpecs.add(branch);
                    }
                }
            }
            String combo = getComboName(partySpecs);
            sb.append(",\"combo\":\"").append(combo).append("\"");
            sb.append("}");
        }
        sb.append("]}");
        ComputerActionHandler.sendResponse(player, "party_view_data", sb.toString(), eco);
    }

    private static String getComboName(java.util.Set<com.ultra.megamod.feature.skills.SkillBranch> specs) {
        var B = com.ultra.megamod.feature.skills.SkillBranch.class;
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.BERSERKER) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.SHIELD_WALL)) return "War Party";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.SHIELD_WALL) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.BLADE_MASTERY)) return "Vanguard";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.MANA_WEAVER) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.ENDURANCE)) return "Healer's Guard";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.RANGED_PRECISION) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.TACTICIAN)) return "Sniper Duo";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.SPELL_BLADE) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.RANGED_PRECISION)) return "Arcane Artillery";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.CROP_MASTER) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.ANIMAL_HANDLER)) return "Nature's Alliance";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.DUNGEONEER) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.EXPLORER)) return "Dungeon Delvers";
        if (specs.contains(com.ultra.megamod.feature.skills.SkillBranch.EFFICIENT_MINING) && specs.contains(com.ultra.megamod.feature.skills.SkillBranch.ORE_FINDER)) return "Mining Expedition";
        return "";
    }

    private static void sendBountyViewData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.adminEnsureLoaded(level);
        java.util.List<com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.Bounty> allBounties =
            com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.getAllBounties();
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder("{\"bounties\":[");
        boolean first = true;
        for (com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler.Bounty b : allBounties) {
            if (!first) sb.append(",");
            first = false;
            long hoursLeft = Math.max(0, (24 * 60 * 60 * 1000L - (now - b.postedTime)) / (60 * 60 * 1000L));
            sb.append("{\"id\":").append(b.id);
            sb.append(",\"poster\":\"").append(b.posterName.replace("\"", "\\\"")).append("\"");
            sb.append(",\"item\":\"").append(b.itemName.replace("\"", "\\\"")).append("\"");
            sb.append(",\"qty\":").append(b.quantity);
            sb.append(",\"price\":").append(b.priceOffered);
            sb.append(",\"fulfilled\":").append(b.fulfilled);
            if (b.fulfilled) {
                sb.append(",\"fulfiller\":\"").append(b.fulfillerName.replace("\"", "\\\"")).append("\"");
            }
            sb.append(",\"hoursLeft\":").append(hoursLeft);
            sb.append("}");
        }
        sb.append("]}");
        ComputerActionHandler.sendResponse(player, "bounty_view_data", sb.toString(), eco);
    }

    private static void sendGameScoresData(ServerPlayer player, EconomyManager eco, ServerLevel level) {
        var mgr = com.ultra.megamod.feature.computer.minigames.MinigameScoreManager.get(level);
        Map<UUID, Map<String, Integer>> all = mgr.getAllScores();
        StringBuilder sb = new StringBuilder("{\"players\":[");
        boolean first = true;
        for (Map.Entry<UUID, Map<String, Integer>> entry : all.entrySet()) {
            UUID uuid = entry.getKey();
            String name = ComputerActionHandler.resolveName(uuid, level);
            if (name == null) continue;
            Map<String, Integer> gs = entry.getValue();
            int snake = gs.getOrDefault("snake", 0);
            int tetris = gs.getOrDefault("tetris", 0);
            int minesweeper = gs.getOrDefault("minesweeper", 0);
            int total = snake + tetris + minesweeper;
            if (total == 0) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"uuid\":\"").append(uuid)
              .append("\",\"name\":\"").append(name.replace("\"", "\\\""))
              .append("\",\"snake\":").append(snake)
              .append(",\"tetris\":").append(tetris)
              .append(",\"minesweeper\":").append(minesweeper)
              .append(",\"total\":").append(total).append("}");
        }
        // Also include online players with no scores yet
        for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
            UUID uuid = sp.getUUID();
            if (all.containsKey(uuid)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"uuid\":\"").append(uuid)
              .append("\",\"name\":\"").append(sp.getGameProfile().name().replace("\"", "\\\""))
              .append("\",\"snake\":0,\"tetris\":0,\"minesweeper\":0,\"total\":0}");
        }
        sb.append("]}");
        ComputerActionHandler.sendResponse(player, "game_scores_data", sb.toString(), eco);
    }

    private static String resolveName(UUID uuid, ServerLevel level) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return uuid.toString().substring(0, 8);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        try {
            int wallet = eco.getWallet(player.getUUID());
            int bank = eco.getBank(player.getUUID());
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to send computer response '{}' to {}: {}", type, player.getGameProfile().name(), e.getMessage(), e);
            // Last-resort: send without wallet/bank data
            try {
                PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, 0, 0));
            } catch (Exception ignored) {}
        }
    }
}

