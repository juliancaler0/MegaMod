/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.util.RandomSource
 */
package com.ultra.megamod.feature.relics.data;

import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;

public enum WeaponRarity {
    COMMON(0, 1, 1.0f, 40, ChatFormatting.GRAY, "Common"),
    UNCOMMON(1, 2, 1.15f, 30, ChatFormatting.GREEN, "Uncommon"),
    RARE(2, 3, 1.3f, 20, ChatFormatting.BLUE, "Rare"),
    MYTHIC(3, 4, 1.5f, 8, ChatFormatting.DARK_PURPLE, "Mythic"),
    LEGENDARY(4, 6, 2.0f, 2, ChatFormatting.GOLD, "Legendary");

    private final int minBonuses;
    private final int maxBonuses;
    private final float damageMultiplier;
    private final int weight;
    private final ChatFormatting nameColor;
    private final String displayName;
    private static final int TOTAL_WEIGHT = 100;

    private WeaponRarity(int minBonuses, int maxBonuses, float damageMultiplier, int weight, ChatFormatting nameColor, String displayName) {
        this.minBonuses = minBonuses;
        this.maxBonuses = maxBonuses;
        this.damageMultiplier = damageMultiplier;
        this.weight = weight;
        this.nameColor = nameColor;
        this.displayName = displayName;
    }

    public int getMinBonuses() {
        return this.minBonuses;
    }

    public int getMaxBonuses() {
        return this.maxBonuses;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public int getWeight() {
        return this.weight;
    }

    public ChatFormatting getNameColor() {
        return this.nameColor;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static WeaponRarity roll(RandomSource random) {
        int roll = random.nextInt(100);
        int cumulative = 0;
        for (WeaponRarity rarity : WeaponRarity.values()) {
            if (roll >= (cumulative += rarity.weight)) continue;
            return rarity;
        }
        return COMMON;
    }

    public int rollBonusCount(RandomSource random) {
        if (this.minBonuses >= this.maxBonuses) {
            return this.minBonuses;
        }
        return this.minBonuses + random.nextInt(this.maxBonuses - this.minBonuses + 1);
    }

    public static WeaponRarity fromOrdinal(int ordinal) {
        WeaponRarity[] values = WeaponRarity.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return COMMON;
    }
}

