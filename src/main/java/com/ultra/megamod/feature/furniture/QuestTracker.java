package com.ultra.megamod.feature.furniture;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.util.*;

/**
 * Tracks accepted Quest Board quests per player. Coin rewards are only paid
 * when the corresponding dungeon tier is completed (boss killed).
 *
 * Persisted via NbtIo in world/data/megamod_quest_tracker.dat
 * Also reads legacy megamod_herald_quests.dat for backwards compatibility.
 */
public class QuestTracker {

    private static QuestTracker INSTANCE;

    private final Map<UUID, List<ActiveQuest>> activeQuests = new HashMap<>();
    private boolean dirty = false;

    public record ActiveQuest(String questId, int difficultyLevel, int coinReward, String title) {}

    public static QuestTracker get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new QuestTracker();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public void acceptQuest(UUID playerUUID, QuestData quest) {
        activeQuests.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                .add(new ActiveQuest(quest.id(), quest.difficultyLevel(), quest.coinReward(), quest.title()));
        dirty = true;
    }

    /**
     * Called when a dungeon is cleared. Returns total coins earned from matching
     * quests for this player/tier, and removes those quests.
     */
    public int completeDungeonForPlayer(UUID playerUUID, int dungeonTier) {
        List<ActiveQuest> quests = activeQuests.get(playerUUID);
        if (quests == null || quests.isEmpty()) return 0;

        int totalCoins = 0;
        List<ActiveQuest> completed = new ArrayList<>();

        for (ActiveQuest q : quests) {
            if (q.difficultyLevel() == dungeonTier) {
                totalCoins += q.coinReward();
                completed.add(q);
            }
        }

        quests.removeAll(completed);
        if (quests.isEmpty()) activeQuests.remove(playerUUID);
        if (!completed.isEmpty()) dirty = true;

        return totalCoins;
    }

    public List<ActiveQuest> getActiveQuests(UUID playerUUID) {
        return activeQuests.getOrDefault(playerUUID, List.of());
    }

    // ---- Persistence ----

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File file = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(),
                    "data/megamod_quest_tracker.dat");
            file.getParentFile().mkdirs();

            CompoundTag root = new CompoundTag();
            ListTag playerList = new ListTag();

            for (var entry : activeQuests.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putString("uuid", entry.getKey().toString());
                ListTag questList = new ListTag();
                for (ActiveQuest q : entry.getValue()) {
                    CompoundTag questTag = new CompoundTag();
                    questTag.putString("id", q.questId());
                    questTag.putInt("diff", q.difficultyLevel());
                    questTag.putInt("coins", q.coinReward());
                    questTag.putString("title", q.title());
                    questList.add(questTag);
                }
                playerTag.put("quests", questList);
                playerList.add(playerTag);
            }
            root.put("players", playerList);
            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save quest tracker", e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        File baseDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();

        // Try new file first, fall back to legacy herald file
        File file = new File(baseDir, "data/megamod_quest_tracker.dat");
        if (!file.exists()) {
            file = new File(baseDir, "data/megamod_herald_quests.dat");
        }
        if (!file.exists()) return;

        try {
            CompoundTag root = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            ListTag playerList = root.getListOrEmpty("players");

            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompoundOrEmpty(i);
                UUID uuid = UUID.fromString(playerTag.getStringOr("uuid", ""));
                ListTag questList = playerTag.getListOrEmpty("quests");
                List<ActiveQuest> quests = new ArrayList<>();
                for (int j = 0; j < questList.size(); j++) {
                    CompoundTag questTag = questList.getCompoundOrEmpty(j);
                    quests.add(new ActiveQuest(
                            questTag.getStringOr("id", ""),
                            questTag.getIntOr("diff", 0),
                            questTag.getIntOr("coins", 0),
                            questTag.getStringOr("title", "")
                    ));
                }
                if (!quests.isEmpty()) activeQuests.put(uuid, quests);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load quest tracker", e);
        }
    }
}
