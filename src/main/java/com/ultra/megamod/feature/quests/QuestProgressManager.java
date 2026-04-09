package com.ultra.megamod.feature.quests;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuestProgressManager {
    private static QuestProgressManager INSTANCE;
    private static final String FILE_NAME = "megamod_quest_progress.dat";

    private final Map<UUID, PlayerQuestProgress> playerData = new HashMap<>();
    private boolean dirty = false;

    public static QuestProgressManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new QuestProgressManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    // ─── Per-player progress data ───

    public static class PlayerQuestProgress {
        public final Set<String> completedQuests = new HashSet<>();
        public final Set<String> claimedQuests = new HashSet<>();
        public final Set<String> trackedQuests = new LinkedHashSet<>(); // max 3, order preserved
        public final Set<String> seenQuests = new HashSet<>();
        // Event-driven counters (for task types not directly polled from other managers)
        public int totalBountiesCompleted;
        public int totalCasinoPlays;
        public int totalMarketplaceTrades;
        public final Set<String> visitedDimensions = new HashSet<>();
    }

    // ─── Access helpers ───

    public PlayerQuestProgress getOrCreate(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerQuestProgress());
    }

    public boolean isCompleted(UUID uuid, String questId) {
        PlayerQuestProgress p = playerData.get(uuid);
        return p != null && p.completedQuests.contains(questId);
    }

    public boolean isRewardClaimed(UUID uuid, String questId) {
        PlayerQuestProgress p = playerData.get(uuid);
        return p != null && p.claimedQuests.contains(questId);
    }

    public boolean isSeen(UUID uuid, String questId) {
        PlayerQuestProgress p = playerData.get(uuid);
        return p != null && p.seenQuests.contains(questId);
    }

    public boolean arePrerequisitesMet(UUID uuid, QuestDefinitions.QuestDef def) {
        if (def.prerequisites().length == 0) return true;
        PlayerQuestProgress p = playerData.get(uuid);
        if (p == null) return def.prerequisites().length == 0;
        for (String prereq : def.prerequisites()) {
            if (!p.completedQuests.contains(prereq)) return false;
        }
        return true;
    }

    public boolean isQuestAvailable(UUID uuid, QuestDefinitions.QuestDef def) {
        return !isCompleted(uuid, def.id()) && arePrerequisitesMet(uuid, def);
    }

    // ─── Progress mutation ───

    public void completeQuest(UUID uuid, String questId) {
        getOrCreate(uuid).completedQuests.add(questId);
        markDirty();
    }

    public void claimRewards(UUID uuid, String questId) {
        getOrCreate(uuid).claimedQuests.add(questId);
        markDirty();
    }

    public void markSeen(UUID uuid, String questId) {
        getOrCreate(uuid).seenQuests.add(questId);
        markDirty();
    }

    // ─── Tracking (HUD) ───

    public Set<String> getTrackedQuests(UUID uuid) {
        PlayerQuestProgress p = playerData.get(uuid);
        return p != null ? p.trackedQuests : Set.of();
    }

    public boolean trackQuest(UUID uuid, String questId) {
        PlayerQuestProgress p = getOrCreate(uuid);
        if (p.trackedQuests.size() >= 3) return false;
        boolean added = p.trackedQuests.add(questId);
        if (added) markDirty();
        return added;
    }

    public void untrackQuest(UUID uuid, String questId) {
        PlayerQuestProgress p = playerData.get(uuid);
        if (p != null && p.trackedQuests.remove(questId)) markDirty();
    }

    // ─── Event-driven counters ───

    public void incrementBountyComplete(UUID uuid) {
        getOrCreate(uuid).totalBountiesCompleted++;
        markDirty();
    }

    public void incrementCasinoPlay(UUID uuid) {
        getOrCreate(uuid).totalCasinoPlays++;
        markDirty();
    }

    public void incrementMarketplaceTrade(UUID uuid) {
        getOrCreate(uuid).totalMarketplaceTrades++;
        markDirty();
    }

    public void addVisitedDimension(UUID uuid, String dimensionId) {
        if (getOrCreate(uuid).visitedDimensions.add(dimensionId)) markDirty();
    }

    // ─── Completion count (for Mastery "questsCompleted" stat) ───

    public int getCompletedCount(UUID uuid) {
        PlayerQuestProgress p = playerData.get(uuid);
        return p != null ? p.completedQuests.size() : 0;
    }

    // ─── Persistence ───

    private void markDirty() { this.dirty = true; }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File dataDir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File file = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            ListTag playerList = new ListTag();
            for (Map.Entry<UUID, PlayerQuestProgress> entry : playerData.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putString("uuid", entry.getKey().toString());
                PlayerQuestProgress p = entry.getValue();

                playerTag.put("completed", toStringList(p.completedQuests));
                playerTag.put("claimed", toStringList(p.claimedQuests));
                playerTag.put("tracked", toStringList(p.trackedQuests));
                playerTag.put("seen", toStringList(p.seenQuests));
                playerTag.putInt("bountiesCompleted", p.totalBountiesCompleted);
                playerTag.putInt("casinoPlays", p.totalCasinoPlays);
                playerTag.putInt("marketplaceTrades", p.totalMarketplaceTrades);
                playerTag.put("visitedDimensions", toStringList(p.visitedDimensions));

                playerList.add(playerTag);
            }
            root.put("players", playerList);
            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save quest progress: {}", e.getMessage(), e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File dataDir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            File file = new File(dataDir, FILE_NAME);
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            ListTag playerList = root.getListOrEmpty("players");
            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompoundOrEmpty(i);
                String uuidStr = playerTag.getStringOr("uuid", "");
                if (uuidStr.isEmpty()) continue;
                UUID uuid = UUID.fromString(uuidStr);
                PlayerQuestProgress p = new PlayerQuestProgress();

                loadStringSet(playerTag, "completed", p.completedQuests);
                loadStringSet(playerTag, "claimed", p.claimedQuests);
                loadStringSet(playerTag, "tracked", p.trackedQuests);
                loadStringSet(playerTag, "seen", p.seenQuests);
                p.totalBountiesCompleted = playerTag.getIntOr("bountiesCompleted", 0);
                p.totalCasinoPlays = playerTag.getIntOr("casinoPlays", 0);
                p.totalMarketplaceTrades = playerTag.getIntOr("marketplaceTrades", 0);
                loadStringSet(playerTag, "visitedDimensions", p.visitedDimensions);

                playerData.put(uuid, p);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load quest progress: {}", e.getMessage(), e);
        }
    }

    // ─── NBT helpers ───

    private static ListTag toStringList(Set<String> set) {
        ListTag list = new ListTag();
        for (String s : set) list.add(StringTag.valueOf(s));
        return list;
    }

    private static void loadStringSet(CompoundTag tag, String key, Set<String> target) {
        ListTag list = tag.getListOrEmpty(key);
        for (int i = 0; i < list.size(); i++) {
            Tag t = list.get(i);
            if (t instanceof StringTag st) {
                target.add(st.value());
            }
        }
    }
}
