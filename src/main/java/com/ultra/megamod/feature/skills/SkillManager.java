/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.storage.LevelResource
 */
package com.ultra.megamod.feature.skills;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class SkillManager {
    private static SkillManager INSTANCE;
    private static final String FILE_NAME = "megamod_skills.dat";
    private final Map<UUID, PlayerSkillData> playerData = new HashMap<UUID, PlayerSkillData>();
    private boolean dirty = false;
    private double adminXpMultiplier = 1.0;
    private double adminOnlyXpBoost = 1.0;

    public double getAdminXpMultiplier() { return adminXpMultiplier; }
    public void setAdminXpMultiplier(double value) { this.adminXpMultiplier = Math.max(1.0, Math.min(value, 10.0)); markDirty(); }

    public double getAdminOnlyXpBoost() { return adminOnlyXpBoost; }
    public void setAdminOnlyXpBoost(double value) { this.adminOnlyXpBoost = Math.max(1.0, Math.min(value, 10.0)); markDirty(); }

    public static SkillManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new SkillManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() {
        this.dirty = true;
    }

    private PlayerSkillData getOrCreate(UUID playerId) {
        return this.playerData.computeIfAbsent(playerId, k -> new PlayerSkillData());
    }

    public int addXp(UUID playerId, SkillTreeType tree, int amount) {
        int required;
        if (amount <= 0) {
            return 0;
        }
        PlayerSkillData data = this.getOrCreate(playerId);
        int currentXp = data.treeXp.getOrDefault((Object)tree, 0);
        int currentLevel = data.treeLevels.getOrDefault((Object)tree, 0);
        int levelsGained = 0;
        currentXp += amount;
        while (currentLevel < 50 && currentXp >= (required = SkillTreeType.xpForLevel(currentLevel))) {
            currentXp -= required;
            ++currentLevel;
            int currentTreePoints = data.availablePoints.getOrDefault(tree, 0);
            data.availablePoints.put(tree, currentTreePoints + 1);
            ++levelsGained;
        }
        if (currentLevel >= 50) {
            currentXp = 0;
        }
        data.treeXp.put(tree, currentXp);
        data.treeLevels.put(tree, currentLevel);
        this.markDirty();
        return levelsGained;
    }

    public int getLevel(UUID playerId, SkillTreeType tree) {
        return this.getOrCreate(playerId).getLevel(tree);
    }

    public int getXp(UUID playerId, SkillTreeType tree) {
        return this.getOrCreate(playerId).getXp(tree);
    }

    public int getAvailablePoints(UUID playerId) {
        PlayerSkillData data = this.getOrCreate(playerId);
        int total = 0;
        for (int pts : data.availablePoints.values()) {
            total += pts;
        }
        return total;
    }

    public int getAvailablePoints(UUID playerId, SkillTreeType tree) {
        return this.getOrCreate(playerId).availablePoints.getOrDefault(tree, 0);
    }

    public boolean isNodeUnlocked(UUID playerId, String nodeId) {
        return this.getOrCreate(playerId).isNodeUnlocked(nodeId);
    }

    public Set<String> getUnlockedNodes(UUID playerId) {
        return new HashSet<String>(this.getOrCreate(playerId).getUnlockedNodes());
    }

    public Set<String> getSpecializedBranches(UUID playerId, SkillTreeType tree) {
        PlayerSkillData data = this.getOrCreate(playerId);
        HashSet<String> specialized = new HashSet<String>();
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node == null || node.branch().getTreeType() != tree) continue;
            specialized.add(node.branch().name());
        }
        return specialized;
    }

    public boolean canUnlockInBranch(UUID playerId, SkillNode node) {
        return canUnlockInBranch(playerId, node, false);
    }

    public boolean canUnlockInBranch(UUID playerId, SkillNode node, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        SkillTreeType tree = node.branch().getTreeType();
        Set<String> specialized = this.getSpecializedBranches(playerId, tree);
        // Prestige 3+ unlocks a 3rd branch specialization slot
        int maxBranches = prestigeThirdBranch(playerId, tree) ? 3 : 2;
        return specialized.size() < maxBranches || specialized.contains(node.branch().name());
    }

    /** Set by SkillEvents on server start / prestige changes */
    private static java.util.function.BiFunction<UUID, SkillTreeType, Boolean> prestigeChecker;

    public static void setPrestigeChecker(java.util.function.BiFunction<UUID, SkillTreeType, Boolean> checker) {
        prestigeChecker = checker;
    }

    private boolean prestigeThirdBranch(UUID playerId, SkillTreeType tree) {
        if (prestigeChecker == null) return false;
        return prestigeChecker.apply(playerId, tree);
    }

    public boolean isBranchLocked(UUID playerId, SkillBranch branch) {
        return isBranchLocked(playerId, branch, false);
    }

    public boolean isBranchLocked(UUID playerId, SkillBranch branch, boolean isAdmin) {
        if (isAdmin) {
            return false;
        }
        Set<String> specialized = this.getSpecializedBranches(playerId, branch.getTreeType());
        int maxBranches = prestigeThirdBranch(playerId, branch.getTreeType()) ? 3 : 2;
        return specialized.size() >= maxBranches && !specialized.contains(branch.name());
    }

    public boolean unlockNode(UUID playerId, String nodeId, boolean isAdmin) {
        PlayerSkillData data = this.getOrCreate(playerId);
        if (data.unlockedNodes.contains(nodeId)) {
            return false;
        }
        SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
        if (node == null) {
            return false;
        }
        SkillTreeType tree = node.branch().getTreeType();
        int treePoints = data.availablePoints.getOrDefault(tree, 0);
        if (treePoints < node.cost()) {
            return false;
        }
        for (String prereq : node.prerequisites()) {
            if (data.unlockedNodes.contains(prereq)) continue;
            return false;
        }
        if (!this.canUnlockInBranch(playerId, node, isAdmin)) {
            return false;
        }
        data.unlockedNodes.add(nodeId);
        data.availablePoints.put(tree, treePoints - node.cost());
        this.markDirty();
        return true;
    }

    public boolean unlockNode(UUID playerId, String nodeId) {
        return unlockNode(playerId, nodeId, false);
    }

    /**
     * Force-unlocks a node without checking points, prerequisites, or branch limits.
     * Used by spell scrolls and admin tools.
     */
    public boolean forceUnlockNode(UUID playerId, String nodeId) {
        PlayerSkillData data = this.getOrCreate(playerId);
        if (data.unlockedNodes.contains(nodeId)) {
            return false;
        }
        data.unlockedNodes.add(nodeId);
        this.markDirty();
        return true;
    }


    public void respec(UUID playerId) {
        PlayerSkillData data = this.getOrCreate(playerId);
        Map<SkillTreeType, Integer> refundedPerTree = new EnumMap<>(SkillTreeType.class);
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node == null) continue;
            SkillTreeType tree = node.branch().getTreeType();
            refundedPerTree.merge(tree, node.cost(), Integer::sum);
        }
        data.unlockedNodes.clear();
        for (Map.Entry<SkillTreeType, Integer> entry : refundedPerTree.entrySet()) {
            int current = data.availablePoints.getOrDefault(entry.getKey(), 0);
            data.availablePoints.put(entry.getKey(), current + entry.getValue());
            data.incrementRespecCount(entry.getKey());
        }
        this.markDirty();
    }

    public int respecBranch(UUID playerId, SkillBranch branch) {
        PlayerSkillData data = this.getOrCreate(playerId);
        SkillTreeType tree = branch.getTreeType();
        int refunded = 0;
        Set<String> toRemove = new HashSet<>();
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node == null) continue;
            if (node.branch() == branch) {
                toRemove.add(nodeId);
                refunded += node.cost();
            }
        }
        if (toRemove.isEmpty()) {
            return 0;
        }
        data.unlockedNodes.removeAll(toRemove);
        int current = data.availablePoints.getOrDefault(tree, 0);
        data.availablePoints.put(tree, current + refunded);
        data.incrementRespecCount(tree);
        this.markDirty();
        return refunded;
    }

    public int getRespecCount(UUID playerId, SkillTreeType tree) {
        return this.getOrCreate(playerId).getRespecCount(tree);
    }

    /**
     * Returns the highest tier unlocked in the given branch for a player.
     */
    public int getHighestTierInBranch(UUID playerId, SkillBranch branch) {
        PlayerSkillData data = this.getOrCreate(playerId);
        int highest = 0;
        for (int t = 1; t <= 5; t++) {
            if (data.unlockedNodes.contains(branch.name().toLowerCase() + "_" + t)) {
                highest = t;
            }
        }
        return highest;
    }

    /**
     * Counts total skill level investment in a specific tree (sum of all unlocked node costs).
     */
    public int getTotalInvestment(UUID playerId, SkillTreeType tree) {
        PlayerSkillData data = this.getOrCreate(playerId);
        int total = 0;
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch().getTreeType() == tree) {
                total += node.cost();
            }
        }
        return total;
    }

    /**
     * Returns the total skill points invested in a specific BRANCH (not tree).
     * Used for class branch investment checks (spell unlock gating, set bonus scaling).
     */
    public int getPointsInBranch(UUID playerId, SkillBranch branch) {
        PlayerSkillData data = this.getOrCreate(playerId);
        int total = 0;
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch() == branch) {
                total += node.cost();
            }
        }
        return total;
    }

    /**
     * Resets respec count for a specific tree (gives player their "first free" respec back).
     */
    public void resetRespecCount(UUID playerId, SkillTreeType tree) {
        PlayerSkillData data = this.getOrCreate(playerId);
        data.respecCount.put(tree, 0);
        this.markDirty();
    }

    /**
     * Resets respec counts for all trees.
     */
    public void resetAllRespecCounts(UUID playerId) {
        PlayerSkillData data = this.getOrCreate(playerId);
        for (SkillTreeType t : SkillTreeType.values()) {
            data.respecCount.put(t, 0);
        }
        this.markDirty();
    }

    public void addPoints(UUID playerId, SkillTreeType tree, int amount) {
        if (amount <= 0) return;
        PlayerSkillData data = this.getOrCreate(playerId);
        int current = data.availablePoints.getOrDefault(tree, 0);
        data.availablePoints.put(tree, current + amount);
        this.markDirty();
    }

    public void setLevel(UUID playerId, SkillTreeType tree, int level) {
        PlayerSkillData data = this.getOrCreate(playerId);
        int clamped = Math.max(0, Math.min(50, level));
        data.treeLevels.put(tree, clamped);
        data.treeXp.put(tree, 0);
        this.markDirty();
    }

    /**
     * Admin max-out: sets all trees to level 50, unlocks every node, zeroes remaining points.
     */
    public void maxOutAllTrees(UUID playerId) {
        PlayerSkillData data = this.getOrCreate(playerId);
        for (SkillTreeType tree : SkillTreeType.values()) {
            data.treeLevels.put(tree, 50);
            data.treeXp.put(tree, 0);
            // Unlock every node in this tree
            for (SkillNode node : SkillTreeDefinitions.getNodesForTree(tree)) {
                data.unlockedNodes.add(node.id());
            }
            // Zero out remaining points (all spent)
            data.availablePoints.put(tree, 0);
        }
        this.markDirty();
    }

    public void resetTree(UUID playerId, SkillTreeType tree) {
        PlayerSkillData data = this.getOrCreate(playerId);
        data.treeLevels.put(tree, 0);
        data.treeXp.put(tree, 0);
        data.availablePoints.put(tree, 0);
        Set<String> toRemove = new HashSet<>();
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node == null) continue;
            if (node.branch().getTreeType() == tree) {
                toRemove.add(nodeId);
            }
        }
        data.unlockedNodes.removeAll(toRemove);
        this.markDirty();
    }

    /**
     * Returns unspent skill points for a tree. These fuel the passive mastery system,
     * granting +0.5% to the tree's primary stat per unspent point.
     */
    public int getUnspentPoints(UUID playerId, SkillTreeType tree) {
        PlayerSkillData data = this.getOrCreate(playerId);
        // Count total points spent in this tree
        int spent = 0;
        for (String nodeId : data.unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch().getTreeType() == tree) {
                spent += node.cost();
            }
        }
        int totalEarned = data.treeLevels.getOrDefault(tree, 0); // 1 point per level
        int available = data.availablePoints.getOrDefault(tree, 0);
        // Unspent = available points that can't be spent on any remaining node
        // For simplicity: just return current available points (they're what's left over)
        return available;
    }

    public Map<UUID, PlayerSkillData> getAllPlayerData() {
        return Collections.unmodifiableMap(this.playerData);
    }

    public boolean checkAntiAbuse(UUID playerId, String source, int amount) {
        PlayerSkillData data = this.getOrCreate(playerId);
        long now = System.currentTimeMillis();
        String key = source + "_time";
        String countKey = source + "_count";
        long lastReset = 0L;
        if (data.antiAbuseTimestamps.containsKey(key)) {
            lastReset = data.antiAbuseTimestamps.get(key);
        }
        if (now - lastReset > 60000L) {
            data.antiAbuseTimestamps.put(key, now);
            data.antiAbuseTimestamps.put(countKey, Long.valueOf(amount));
            return true;
        }
        long accumulated = data.antiAbuseTimestamps.getOrDefault(countKey, 0L);
        if (accumulated + (long)amount > 100L) {
            return false;
        }
        data.antiAbuseTimestamps.put(countKey, accumulated + (long)amount);
        return true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                this.adminXpMultiplier = root.contains("adminXpMultiplier") ? root.getDoubleOr("adminXpMultiplier", 1.0) : 1.0;
                this.adminOnlyXpBoost = root.contains("adminOnlyXpBoost") ? root.getDoubleOr("adminOnlyXpBoost", 1.0) : 1.0;
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pTag = players.getCompoundOrEmpty(key);
                    PlayerSkillData data = new PlayerSkillData();
                    CompoundTag levelsTag = pTag.getCompoundOrEmpty("levels");
                    CompoundTag xpTag = pTag.getCompoundOrEmpty("xp");
                    for (SkillTreeType type : SkillTreeType.values()) {
                        String typeName = type.name().toLowerCase();
                        data.treeLevels.put(type, levelsTag.getIntOr(typeName, 0));
                        data.treeXp.put(type, xpTag.getIntOr(typeName, 0));
                    }
                    // Load per-tree points; fall back to legacy single "points" field
                    CompoundTag pointsTag = pTag.getCompoundOrEmpty("tree_points");
                    if (!pointsTag.keySet().isEmpty()) {
                        for (SkillTreeType pt : SkillTreeType.values()) {
                            data.availablePoints.put(pt, pointsTag.getIntOr(pt.name().toLowerCase(), 0));
                        }
                    } else {
                        // Legacy migration: distribute old global points evenly, remainder to COMBAT
                        int legacyPoints = pTag.getIntOr("points", 0);
                        if (legacyPoints > 0) {
                            SkillTreeType[] trees = SkillTreeType.values();
                            int perTree = legacyPoints / trees.length;
                            int remainder = legacyPoints % trees.length;
                            for (SkillTreeType pt : trees) {
                                data.availablePoints.put(pt, perTree);
                            }
                            data.availablePoints.put(SkillTreeType.COMBAT,
                                data.availablePoints.getOrDefault(SkillTreeType.COMBAT, 0) + remainder);
                        }
                    }
                    CompoundTag nodesTag = pTag.getCompoundOrEmpty("nodes");
                    for (String nodeKey : nodesTag.keySet()) {
                        if (!nodesTag.getBooleanOr(nodeKey, false)) continue;
                        data.unlockedNodes.add(nodeKey);
                    }
                    CompoundTag respecTag = pTag.getCompoundOrEmpty("respec_counts");
                    for (SkillTreeType rt : SkillTreeType.values()) {
                        data.respecCount.put(rt, respecTag.getIntOr(rt.name().toLowerCase(), 0));
                    }
                    this.playerData.put(uuid, data);
                }
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load skill data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putDouble("adminXpMultiplier", this.adminXpMultiplier);
            root.putDouble("adminOnlyXpBoost", this.adminOnlyXpBoost);
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, PlayerSkillData> entry : this.playerData.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                PlayerSkillData data = entry.getValue();
                CompoundTag levelsTag = new CompoundTag();
                CompoundTag xpTag = new CompoundTag();
                for (SkillTreeType type : SkillTreeType.values()) {
                    String typeName = type.name().toLowerCase();
                    levelsTag.putInt(typeName, data.treeLevels.getOrDefault((Object)type, 0).intValue());
                    xpTag.putInt(typeName, data.treeXp.getOrDefault((Object)type, 0).intValue());
                }
                pTag.put("levels", (Tag)levelsTag);
                pTag.put("xp", (Tag)xpTag);
                // Save per-tree points
                CompoundTag pointsTag = new CompoundTag();
                int totalPoints = 0;
                for (SkillTreeType pt : SkillTreeType.values()) {
                    int pts = data.availablePoints.getOrDefault(pt, 0);
                    pointsTag.putInt(pt.name().toLowerCase(), pts);
                    totalPoints += pts;
                }
                pTag.put("tree_points", (Tag)pointsTag);
                pTag.putInt("points", totalPoints); // keep legacy field for backward compat
                CompoundTag nodesTag = new CompoundTag();
                for (String nodeId : data.unlockedNodes) {
                    nodesTag.putBoolean(nodeId, true);
                }
                pTag.put("nodes", (Tag)nodesTag);
                CompoundTag respecTag = new CompoundTag();
                for (SkillTreeType rt : SkillTreeType.values()) {
                    respecTag.putInt(rt.name().toLowerCase(), data.respecCount.getOrDefault(rt, 0));
                }
                pTag.put("respec_counts", (Tag)respecTag);
                players.put(entry.getKey().toString(), (Tag)pTag);
            }
            root.put("players", (Tag)players);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save skill data", e);
        }
    }

    public static class PlayerSkillData {
        private final Map<SkillTreeType, Integer> treeLevels = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
        private final Map<SkillTreeType, Integer> treeXp = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
        private final Set<String> unlockedNodes = new HashSet<String>();
        private final Map<SkillTreeType, Integer> availablePoints = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
        private final Map<String, Long> antiAbuseTimestamps = new HashMap<String, Long>();
        private final Map<SkillTreeType, Integer> respecCount = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);

        public PlayerSkillData() {
            for (SkillTreeType type : SkillTreeType.values()) {
                this.treeLevels.put(type, 0);
                this.treeXp.put(type, 0);
                this.availablePoints.put(type, 0);
                this.respecCount.put(type, 0);
            }
        }

        public int getLevel(SkillTreeType type) {
            return this.treeLevels.getOrDefault((Object)type, 0);
        }

        public int getXp(SkillTreeType type) {
            return this.treeXp.getOrDefault((Object)type, 0);
        }

        public int getAvailablePoints() {
            int total = 0;
            for (int pts : this.availablePoints.values()) {
                total += pts;
            }
            return total;
        }

        public int getAvailablePoints(SkillTreeType tree) {
            return this.availablePoints.getOrDefault(tree, 0);
        }

        public Set<String> getUnlockedNodes() {
            return this.unlockedNodes;
        }

        public boolean isNodeUnlocked(String nodeId) {
            return this.unlockedNodes.contains(nodeId);
        }

        public int getRespecCount(SkillTreeType tree) {
            return this.respecCount.getOrDefault(tree, 0);
        }

        public void incrementRespecCount(SkillTreeType tree) {
            this.respecCount.put(tree, this.respecCount.getOrDefault(tree, 0) + 1);
        }
    }
}

