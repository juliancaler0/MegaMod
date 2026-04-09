/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 */
package com.ultra.megamod.feature.relics.data;

import net.minecraft.util.RandomSource;

public record RelicStat(String name, double minValue, double maxValue, ScaleType scaleType, double scaleAmount) {
    public double rollInitialValue(RandomSource random) {
        return this.minValue + (this.maxValue - this.minValue) * random.nextDouble();
    }

    public double calculateValue(double baseValue, int points) {
        return switch (this.scaleType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> baseValue + this.scaleAmount * (double)points;
            case 1 -> baseValue * (1.0 + this.scaleAmount * (double)points);
            case 2 -> baseValue * Math.pow(1.0 + this.scaleAmount, points);
        };
    }

    public static enum ScaleType {
        ADD,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL;

    }
}

