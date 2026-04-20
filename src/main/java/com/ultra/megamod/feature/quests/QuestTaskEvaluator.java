package com.ultra.megamod.feature.quests;

import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.ClaimData;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.dungeons.DungeonQuestManager;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.multiplayer.PlayerStatistics;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestTask;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestTaskType;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Evaluates quest task progress by querying existing mod managers.
 * All methods are static — no state is held here.
 */
public class QuestTaskEvaluator {

    /**
     * Returns the current progress value for a given task.
     * The quest is considered complete for this task when the returned value >= task.targetAmount().
     */
    public static int evaluate(ServerPlayer player, QuestTask task, ServerLevel level) {
        UUID uuid = player.getUUID();
        try {
            return switch (task.type()) {
                case HAVE_ITEM -> countItemInInventory(player, task.targetId());
                case REACH_BALANCE -> {
                    EconomyManager eco = EconomyManager.get(level);
                    yield eco.getWallet(uuid) + eco.getBank(uuid);
                }
                case SKILL_LEVEL -> evaluateSkillLevel(player, task.targetId());
                case UNLOCK_SKILL_NODE -> evaluateUnlockSkillNode(player, task.targetId());
                case DUNGEON_CLEAR -> evaluateDungeonClear(uuid, task.targetId(), level);
                case MOB_KILLS -> PlayerStatistics.get(level).getStat(uuid, "mobKills");
                case BLOCKS_BROKEN -> PlayerStatistics.get(level).getStat(uuid, "blocksBroken");
                case STAT_CHECK -> evaluateStatCheck(uuid, task.targetId(), level);
                case MUSEUM_DONATIONS -> countMuseumDonations(uuid, level);
                case CITIZEN_COUNT -> CitizenManager.get(level).getCitizenCount(uuid);
                case CLAIM_CHUNKS -> countClaimedChunks(uuid, level);
                case EQUIP_ACCESSORY -> countEquippedAccessories(uuid, level);
                case PRESTIGE_TREE -> evaluatePrestige(uuid, task.targetId(), level);
                case CHECKMARK -> {
                    // Checkmarks are stored in QuestProgressManager as completed quest tasks
                    // They're manually triggered, not polled — so return from progress data
                    QuestProgressManager qpm = QuestProgressManager.get(level);
                    yield qpm.isCompleted(uuid, "") ? 1 : 0; // handled specially in handler
                }
                case BOUNTY_COMPLETE -> QuestProgressManager.get(level).getOrCreate(uuid).totalBountiesCompleted;
                case CASINO_PLAY -> {
                    // Read directly from CasinoManager stats (STAT_GAMES_PLAYED is at index 3)
                    int[] casinoStats = CasinoManager.get(level).getStats(uuid);
                    yield casinoStats[3]; // STAT_GAMES_PLAYED
                }
                case TRADE_MARKETPLACE -> QuestProgressManager.get(level).getOrCreate(uuid).totalMarketplaceTrades;
                case VISIT_DIMENSION -> QuestProgressManager.get(level).getOrCreate(uuid).visitedDimensions.size();
                case BUILDING_PLACED -> countBuildingPlaced(uuid, task.targetId(), level);
                case BUILDING_COUNT -> countTotalBuildings(uuid, level);
                case BUILDING_LEVEL -> getBuildingLevel(uuid, task.targetId(), level);
                case SURVIVE_RAID -> PlayerStatistics.get(level).getStat(uuid, "raidsDefended");
            };
        } catch (Exception e) {
            return 0; // manager not loaded yet
        }
    }

    // ─── Item counting ───

    private static int countItemInInventory(ServerPlayer player, String itemId) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (id.equals(itemId)) count += stack.getCount();
            }
        }
        return count;
    }

    // ─── Skill level evaluation ───

    /// targetId conventions (same rules as the other skill payloads):
    ///   "ANY" → max level across every Pufferfish category
    ///   "ALL" → min level across every Pufferfish category
    ///   "<category-id>" or legacy "COMBAT"/"ARCANE"/etc. → that specific category
    private static int evaluateSkillLevel(ServerPlayer player, String targetId) {
        var cats = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.allCategoryIds();
        if ("ANY".equals(targetId) || "ALL".equals(targetId)) {
            int best = "ANY".equals(targetId) ? 0 : Integer.MAX_VALUE;
            for (var catId : cats) {
                int lvl = com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI.getCategory(catId)
                        .flatMap(c -> c.getExperience())
                        .map(exp -> exp.getLevel(player))
                        .orElse(0);
                best = "ANY".equals(targetId) ? Math.max(best, lvl) : Math.min(best, lvl);
            }
            return best == Integer.MAX_VALUE ? 0 : best;
        }
        var catId = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.categoryFor(targetId);
        return com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI.getCategory(catId)
                .flatMap(c -> c.getExperience())
                .map(exp -> exp.getLevel(player))
                .orElse(0);
    }

    /// targetId is "<category>/<skill-id>" — returns 1 if that node is unlocked, 0 otherwise.
    /// Plain skill id is accepted and searched across all categories.
    private static int evaluateUnlockSkillNode(ServerPlayer player, String targetId) {
        String skillId = targetId;
        net.minecraft.resources.Identifier catHint = null;
        int slash = targetId.indexOf('/');
        if (slash > 0) {
            catHint = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.categoryFor(targetId.substring(0, slash));
            skillId = targetId.substring(slash + 1);
        }
        var cats = catHint != null
                ? new net.minecraft.resources.Identifier[] { catHint }
                : com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.allCategoryIds();
        for (var catId : cats) {
            var cat = com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI.getCategory(catId);
            if (cat.isEmpty()) continue;
            final String needle = skillId;
            if (cat.get().streamUnlockedSkills(player).anyMatch(sk -> sk.getId().equals(needle))) {
                return 1;
            }
        }
        return 0;
    }

    // ─── Dungeon clear evaluation ───

    private static int evaluateDungeonClear(UUID uuid, String targetId, ServerLevel level) {
        DungeonQuestManager dqm = DungeonQuestManager.get(level);
        if ("ANY".equals(targetId)) {
            return dqm.getTotalClears(uuid);
        } else {
            try {
                return dqm.getClearCount(uuid, DungeonTier.valueOf(targetId));
            } catch (IllegalArgumentException e) {
                return 0;
            }
        }
    }

    // ─── Stat check evaluation ───

    private static int evaluateStatCheck(UUID uuid, String statKey, ServerLevel level) {
        if ("consecutiveClears".equals(statKey)) {
            // Read from DungeonQuestManager — consecutive streak
            try {
                DungeonQuestManager dqm = DungeonQuestManager.get(level);
                return dqm.getTotalClears(uuid); // fallback: use total if no consecutive method
            } catch (Exception e) {
                return 0;
            }
        } else if ("questsCompleted".equals(statKey)) {
            return QuestProgressManager.get(level).getCompletedCount(uuid);
        } else {
            return PlayerStatistics.get(level).getStat(uuid, statKey);
        }
    }

    // ─── Museum donations ───

    private static int countMuseumDonations(UUID uuid, ServerLevel level) {
        MuseumData museum = MuseumData.get(level);
        int total = 0;
        total += museum.getDonatedItems(uuid).size();
        total += museum.getDonatedMobs(uuid).size();
        total += museum.getDonatedArt(uuid).size();
        return total;
    }

    // ─── Claim chunk count ───

    private static int countClaimedChunks(UUID uuid, ServerLevel level) {
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(uuid);
        if (factionId == null) return 0;
        ClaimData claim = ClaimManager.get(level).getClaim(factionId);
        return claim != null ? claim.getChunkCount() : 0;
    }

    // ─── Equipped accessories ───

    private static int countEquippedAccessories(UUID uuid, ServerLevel level) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return 0;
        Map<AccessorySlotType, ItemStack> equipped = LibAccessoryLookup.getAllEquipped(player);
        int count = 0;
        for (ItemStack stack : equipped.values()) {
            if (stack != null && !stack.isEmpty()) count++;
        }
        return count;
    }

    // ─── Colony building evaluation ───

    private static int countBuildingPlaced(UUID uuid, String buildingId, ServerLevel level) {
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(uuid);
        if (factionId == null) return 0;
        // Scan for TileEntityColonyBuilding with matching building ID
        var center = fm.getFaction(factionId).getTownChestPos();
        if (center == null || center.equals(net.minecraft.core.BlockPos.ZERO)) return 0;
        int count = 0;
        int radius = 128;
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                for (int y = -16; y <= 16; y += 4) {
                    var pos = center.offset(x, y, z);
                    var be = level.getBlockEntity(pos);
                    if (be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile
                            && tile.getBuildingId().equals(buildingId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int countTotalBuildings(UUID uuid, ServerLevel level) {
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(uuid);
        if (factionId == null) return 0;
        var center = fm.getFaction(factionId).getTownChestPos();
        if (center == null || center.equals(net.minecraft.core.BlockPos.ZERO)) return 0;
        java.util.Set<String> uniqueTypes = new java.util.HashSet<>();
        int radius = 128;
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                for (int y = -16; y <= 16; y += 4) {
                    var pos = center.offset(x, y, z);
                    var be = level.getBlockEntity(pos);
                    if (be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile
                            && !tile.getBuildingId().isEmpty()) {
                        uniqueTypes.add(tile.getBuildingId());
                    }
                }
            }
        }
        return uniqueTypes.size();
    }

    private static int getBuildingLevel(UUID uuid, String buildingId, ServerLevel level) {
        FactionManager fm = FactionManager.get(level);
        String factionId = fm.getPlayerFaction(uuid);
        if (factionId == null) return 0;
        var center = fm.getFaction(factionId).getTownChestPos();
        if (center == null || center.equals(net.minecraft.core.BlockPos.ZERO)) return 0;
        int maxLevel = 0;
        int radius = 128;
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                for (int y = -16; y <= 16; y += 4) {
                    var pos = center.offset(x, y, z);
                    var be = level.getBlockEntity(pos);
                    if (be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile
                            && tile.getBuildingId().equals(buildingId)) {
                        maxLevel = Math.max(maxLevel, tile.getBuildingLevel());
                    }
                }
            }
        }
        return maxLevel;
    }

    // ─── Prestige evaluation ───

    private static int evaluatePrestige(UUID uuid, String targetId, ServerLevel level) {
        PrestigeManager pm = PrestigeManager.get(level);
        var cats = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.allCategoryIds();
        if ("ANY".equals(targetId)) {
            for (var catId : cats) if (pm.getPrestigeLevel(uuid, catId) > 0) return 1;
            return 0;
        } else if ("ALL".equals(targetId)) {
            for (var catId : cats) if (pm.getPrestigeLevel(uuid, catId) == 0) return 0;
            return 1;
        } else {
            // targetId may be a category id, bare slug, or legacy enum name — normalize.
            var catId = com.ultra.megamod.feature.skills.adminbridge.SkillAdminBridge.categoryFor(targetId);
            return pm.getPrestigeLevel(uuid, catId);
        }
    }
}
