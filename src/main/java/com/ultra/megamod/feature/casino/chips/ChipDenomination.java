package com.ultra.megamod.feature.casino.chips;

/**
 * Casino chip denominations with colors matching real casino standards.
 */
public enum ChipDenomination {
    CHIP_1(1, "1", 0xFFEEEEEE, 0xFFCCCCCC),       // White
    CHIP_3(3, "3", 0xFFFF88AA, 0xFFDD6688),         // Pink
    CHIP_5(5, "5", 0xFFDD3333, 0xFFBB2222),         // Red
    CHIP_10(10, "10", 0xFF3355CC, 0xFF2244AA),       // Blue
    CHIP_20(20, "20", 0xFFDDCC33, 0xFFBBAA22),       // Yellow
    CHIP_50(50, "50", 0xFFEE8833, 0xFFCC6622),       // Orange
    CHIP_100(100, "100", 0xFF222222, 0xFF111111),     // Black
    CHIP_500(500, "500", 0xFF9944CC, 0xFF7733AA),     // Purple
    CHIP_1000(1000, "1K", 0xFFD4AF37, 0xFFB8962E);   // Gold

    public final int value;
    public final String label;
    public final int color;       // main chip color
    public final int borderColor; // darker ring

    ChipDenomination(int value, String label, int color, int borderColor) {
        this.value = value;
        this.label = label;
        this.color = color;
        this.borderColor = borderColor;
    }

    public static ChipDenomination fromValue(int value) {
        for (ChipDenomination d : values()) {
            if (d.value == value) return d;
        }
        return null;
    }

    /** Get the best denomination breakdown for a total amount (fewest chips). */
    public static int[] breakdown(int totalAmount) {
        int[] counts = new int[values().length];
        int remaining = totalAmount;
        // Work from highest to lowest denomination
        ChipDenomination[] denoms = values();
        for (int i = denoms.length - 1; i >= 0 && remaining > 0; i--) {
            counts[i] = remaining / denoms[i].value;
            remaining -= counts[i] * denoms[i].value;
        }
        return counts;
    }
}
