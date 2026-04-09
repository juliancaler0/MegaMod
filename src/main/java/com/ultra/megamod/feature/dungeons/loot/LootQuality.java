package com.ultra.megamod.feature.dungeons.loot;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import net.minecraft.util.RandomSource;

public enum LootQuality {
    COMMON(1, -5592406, ""),
    UNCOMMON(2, -11141291, "Fine"),
    RARE(3, -11184641, "Superior"),
    EPIC(4, -5635926, "Exquisite"),
    LEGENDARY(5, -22016, "Legendary");

    private final int modifierCount;
    private final int nameColor;
    private final String prefix;

    private LootQuality(int modifierCount, int nameColor, String prefix) {
        this.modifierCount = modifierCount;
        this.nameColor = nameColor;
        this.prefix = prefix;
    }

    public int getModifierCount() { return this.modifierCount; }
    public int getNameColor() { return this.nameColor; }
    public String getPrefix() { return this.prefix; }

    /**
     * Configurable quality weights per tier. Index: [tierLevel-1][qualityOrdinal].
     * Values are cumulative thresholds out of 100.
     * E.g. {40, 75, 95, 100, 100} means: 40% Common, 35% Uncommon, 20% Rare, 5% Epic, 0% Legendary.
     * Editable via admin panel at runtime.
     */
    private static final int[][] QUALITY_THRESHOLDS = {
            // Normal:    40% Common, 35% Uncommon, 20% Rare, 5% Epic, 0% Legendary
            {40, 75, 95, 100, 100},
            // Hard:      20% Common, 35% Uncommon, 30% Rare, 12% Epic, 3% Legendary
            {20, 55, 85, 97, 100},
            // Nightmare: 5% Common, 20% Uncommon, 40% Rare, 25% Epic, 10% Legendary
            {5, 25, 65, 90, 100},
            // Infernal:  0% Common, 10% Uncommon, 30% Rare, 40% Epic, 20% Legendary
            {0, 10, 40, 80, 100},
            // Mythic:    0% Common, 0% Uncommon, 15% Rare, 50% Epic, 35% Legendary
            {0, 0, 15, 65, 100},
            // Eternal:   0% Common, 0% Uncommon, 0% Rare, 40% Epic, 60% Legendary
            {0, 0, 0, 40, 100},
    };

    /**
     * Get the quality thresholds array for a tier (for admin panel display/editing).
     * Returns the cumulative threshold array [common, uncommon, rare, epic, legendary].
     */
    public static int[] getThresholds(DungeonTier tier) {
        return QUALITY_THRESHOLDS[tier.getLevel() - 1];
    }

    /**
     * Get the percentage for a specific quality at a specific tier.
     */
    public static int getPercent(DungeonTier tier, LootQuality quality) {
        int[] t = QUALITY_THRESHOLDS[tier.getLevel() - 1];
        int idx = quality.ordinal();
        if (idx == 0) return t[0];
        return t[idx] - t[idx - 1];
    }

    /**
     * Set the percentage for a specific quality at a specific tier.
     * Automatically adjusts neighboring qualities to keep total at 100%.
     */
    public static void setPercent(DungeonTier tier, LootQuality quality, int newPercent) {
        int tierIdx = tier.getLevel() - 1;
        int qualIdx = quality.ordinal();
        newPercent = Math.max(0, Math.min(100, newPercent));

        // Calculate current percentages
        int[] percents = new int[5];
        int[] t = QUALITY_THRESHOLDS[tierIdx];
        percents[0] = t[0];
        for (int i = 1; i < 5; i++) percents[i] = t[i] - t[i - 1];

        int oldPercent = percents[qualIdx];
        int diff = newPercent - oldPercent;
        if (diff == 0) return;

        percents[qualIdx] = newPercent;

        // Redistribute the difference across other qualities
        // Take from/give to the nearest neighbor that has room
        int remaining = -diff;
        // Try neighbors outward from the changed index
        for (int dist = 1; dist < 5 && remaining != 0; dist++) {
            for (int dir = -1; dir <= 1; dir += 2) {
                int neighbor = qualIdx + dir * dist;
                if (neighbor < 0 || neighbor >= 5 || neighbor == qualIdx) continue;
                if (remaining > 0) {
                    int give = Math.min(remaining, 100 - percents[neighbor]);
                    percents[neighbor] += give;
                    remaining -= give;
                } else {
                    int take = Math.min(-remaining, percents[neighbor]);
                    percents[neighbor] -= take;
                    remaining += take;
                }
                if (remaining == 0) break;
            }
        }

        // Rebuild cumulative thresholds
        int cumulative = 0;
        for (int i = 0; i < 5; i++) {
            cumulative += Math.max(0, percents[i]);
            QUALITY_THRESHOLDS[tierIdx][i] = Math.min(100, cumulative);
        }
        // Force last to 100
        QUALITY_THRESHOLDS[tierIdx][4] = 100;
    }

    public static LootQuality rollForTier(DungeonTier tier, RandomSource random) {
        return rollForTier(tier, random, 0.0);
    }

    /**
     * Roll loot quality with a fortune bonus that improves chances.
     * Each point of lootFortune shifts the roll down by 1 (lower roll = better quality).
     * E.g. 20 loot_fortune means the roll is effectively 20 points better.
     */
    public static LootQuality rollForTier(DungeonTier tier, RandomSource random, double lootFortune) {
        int roll = random.nextInt(100);
        roll = Math.max(0, roll - (int) lootFortune);
        int[] t = QUALITY_THRESHOLDS[tier.getLevel() - 1];
        if (roll < t[0]) return COMMON;
        if (roll < t[1]) return UNCOMMON;
        if (roll < t[2]) return RARE;
        if (roll < t[3]) return EPIC;
        return LEGENDARY;
    }
}
