package com.ultra.megamod.feature.casino.wheel;

import java.util.Random;

public enum WheelSegment {
    // Weights match the 30-slice wheel texture distribution
    X1(1, 130, 0xFF4CAF50, "1x"),        // 13/30 = 43.3%
    X2(2, 40, 0xFF2196F3, "2x"),         // 4/30 = 13.3%
    X3(3, 60, 0xFFFFC107, "3x"),         // 6/30 = 20.0%
    X5(5, 40, 0xFFFF9800, "5x"),         // 4/30 = 13.3%
    X10(10, 20, 0xFFE91E63, "10x"),      // 2/30 = 6.7%
    JACKPOT(20, 10, 0xFFC80000, "JACKPOT"); // 1/30 = 3.3% (20x multiplier, RED)

    public final int multiplier;
    public final int weight;
    public final int color;
    public final String displayName;

    WheelSegment(int multiplier, int weight, int color, String displayName) {
        this.multiplier = multiplier;
        this.weight = weight;
        this.color = color;
        this.displayName = displayName;
    }

    /**
     * Returns the sum of all segment weights.
     */
    public static int getTotalWeight() {
        int total = 0;
        for (WheelSegment seg : values()) {
            total += seg.weight;
        }
        return total;
    }

    /**
     * Picks a random segment using weighted selection.
     */
    public static WheelSegment pickRandom(Random random) {
        int totalWeight = getTotalWeight();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WheelSegment seg : values()) {
            cumulative += seg.weight;
            if (roll < cumulative) {
                return seg;
            }
        }
        // Fallback (should never happen)
        return X1;
    }
}
