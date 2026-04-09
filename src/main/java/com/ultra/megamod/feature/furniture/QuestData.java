package com.ultra.megamod.feature.furniture;

/**
 * Represents a quest available on the Quest Board.
 * Players accept quests to receive dungeon keys; coin rewards are paid on dungeon completion.
 */
public record QuestData(
    String id,
    String title,
    String difficulty,
    String dungeonName,
    int coinReward,
    String keyItemId,
    int difficultyLevel // 0=Normal, 1=Hard, 2=Nightmare, 3=Infernal
) {}
