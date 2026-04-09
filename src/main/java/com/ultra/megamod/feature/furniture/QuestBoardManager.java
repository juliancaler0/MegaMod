package com.ultra.megamod.feature.furniture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages global quests available at all Quest Board blocks.
 * Quests refresh every hour (72000 ticks), shared across all boards.
 */
public class QuestBoardManager {

    private static final int QUEST_REFRESH_TICKS = 72000; // 1 hour
    private static final List<QuestData> activeQuests = new ArrayList<>();
    private static long lastRefreshTick = -1;

    private static final String[] DIFFICULTIES = {"Normal", "Hard", "Nightmare", "Infernal"};
    private static final String[] DUNGEON_NAMES = {
        "Forgotten Crypt", "Shadow Cavern", "Infernal Pit", "Frost Tomb",
        "Cursed Mine", "Obsidian Fortress", "Nether Labyrinth", "End Vault",
        "Ancient Temple", "Dragon's Den", "Wither Sanctum", "Ender Maze"
    };

    public static List<QuestData> getQuests(long serverTick) {
        if (lastRefreshTick == -1 || serverTick - lastRefreshTick >= QUEST_REFRESH_TICKS || activeQuests.isEmpty()) {
            lastRefreshTick = serverTick;
            generateQuests();
        }
        return Collections.unmodifiableList(activeQuests);
    }

    private static void generateQuests() {
        activeQuests.clear();
        Random rand = new Random(lastRefreshTick * 47 + 7919); // Different seed from herald entities

        int questCount = 6 + rand.nextInt(5); // 6-10 quests (more than herald since boards are stationary)

        for (int i = 0; i < questCount; i++) {
            int diffIdx = rand.nextInt(DIFFICULTIES.length);
            String difficulty = DIFFICULTIES[diffIdx];
            String dungeonName = DUNGEON_NAMES[rand.nextInt(DUNGEON_NAMES.length)];

            int coinReward = switch (diffIdx) {
                case 0 -> 100 + rand.nextInt(100);    // Normal: 100-199 MC
                case 1 -> 250 + rand.nextInt(250);    // Hard: 250-499 MC
                case 2 -> 500 + rand.nextInt(500);    // Nightmare: 500-999 MC
                case 3 -> 1000 + rand.nextInt(1000);  // Infernal: 1000-1999 MC
                default -> 100;
            };

            String keyItem = switch (diffIdx) {
                case 0 -> "dungeon_key_normal";
                case 1 -> "dungeon_key_hard";
                case 2 -> "dungeon_key_nightmare";
                case 3 -> "dungeon_key_infernal";
                default -> "dungeon_key_normal";
            };

            activeQuests.add(new QuestData(
                "qb_" + lastRefreshTick + "_" + i,
                "Clear " + dungeonName,
                difficulty,
                dungeonName,
                coinReward,
                keyItem,
                diffIdx
            ));
        }
    }

    public static void forceRefresh(long serverTick) {
        lastRefreshTick = serverTick;
        generateQuests();
    }

    public static void removeQuest(String questId) {
        activeQuests.removeIf(q -> q.id().equals(questId));
    }
}
