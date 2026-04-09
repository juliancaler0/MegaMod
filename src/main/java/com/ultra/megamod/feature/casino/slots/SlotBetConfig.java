package com.ultra.megamod.feature.casino.slots;

public class SlotBetConfig {
    public static final int[] BET_VALUES = {1, 5, 10, 25, 50, 100, 250, 500, 1000};
    public static final int[] LINE_MULTIPLIERS = {1, 3, 5};

    /**
     * Gets the bet value at the given index, clamped to valid range.
     */
    public static int getBetValue(int index) {
        if (index < 0) index = 0;
        if (index >= BET_VALUES.length) index = BET_VALUES.length - 1;
        return BET_VALUES[index];
    }

    /**
     * Gets the line multiplier for the given line mode, clamped to valid range.
     * lineMode 0 = 1 line, lineMode 1 = 3 lines, lineMode 2 = 5 lines.
     */
    public static int getLineMultiplier(int lineMode) {
        if (lineMode < 0) lineMode = 0;
        if (lineMode >= LINE_MULTIPLIERS.length) lineMode = LINE_MULTIPLIERS.length - 1;
        return LINE_MULTIPLIERS[lineMode];
    }

    /**
     * Gets the total bet (bet value * line multiplier).
     */
    public static int getTotalBet(int betIndex, int lineMode) {
        return getBetValue(betIndex) * getLineMultiplier(lineMode);
    }
}
