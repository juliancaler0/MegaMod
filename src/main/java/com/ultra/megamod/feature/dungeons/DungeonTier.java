/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.dungeons;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public enum DungeonTier {
    NORMAL(1, 1.0f, 5, 7, "Normal"),
    HARD(2, 1.5f, 6, 8, "Hard"),
    NIGHTMARE(3, 2.5f, 7, 10, "Nightmare"),
    INFERNAL(4, 4.0f, 9, 13, "Infernal"),
    MYTHIC(5, 6.0f, 11, 15, "Mythic"),
    ETERNAL(6, 10.0f, 13, 18, "Eternal");

    private final int level;
    private final float difficultyMultiplier;
    private final int minRooms;
    private final int maxRooms;
    private final String displayName;

    private DungeonTier(int level, float difficultyMultiplier, int minRooms, int maxRooms, String displayName) {
        this.level = level;
        this.difficultyMultiplier = difficultyMultiplier;
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.displayName = displayName;
    }

    public int getLevel() {
        return this.level;
    }

    public float getDifficultyMultiplier() {
        return this.difficultyMultiplier;
    }

    public int getMinRooms() {
        return this.minRooms;
    }

    public int getMaxRooms() {
        return this.maxRooms;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static DungeonTier fromLevel(int level) {
        for (DungeonTier tier : DungeonTier.values()) {
            if (tier.level != level) continue;
            return tier;
        }
        return NORMAL;
    }

    public static DungeonTier fromName(String name) {
        for (DungeonTier tier : DungeonTier.values()) {
            if (!tier.name().equalsIgnoreCase(name)) continue;
            return tier;
        }
        return NORMAL;
    }

    /**
     * Returns the jigsaw BFS depth for dungeon generation.
     * Higher depth = more rooms placed before the boss room.
     */
    /**
     * Jigsaw BFS depth — controls how many rooms branch out.
     * DNL default is 30 for overworld; we scale per tier.
     */
    public int getJigsawDepth() {
        // Each depth level includes rooms + all their child decor/mob/spawner jigsaw elements.
        // A depth of 7 produces ~5-7 rooms with all their decorations.
        return switch (this) {
            case NORMAL    -> 7;
            case HARD      -> 9;
            case NIGHTMARE -> 11;
            case INFERNAL  -> 14;
            case MYTHIC    -> 17;
            case ETERNAL   -> 20;
        };
    }

    /**
     * Max block distance from dungeon origin.
     * DNL default is 116. Bigger tiers spread wider.
     */
    public int getMaxDistance() {
        return switch (this) {
            case NORMAL    -> 116;
            case HARD      -> 116;
            case NIGHTMARE -> 116;
            case INFERNAL  -> 116;
            case MYTHIC    -> 140;
            case ETERNAL   -> 160;
        };
    }

    /**
     * Max jigsaw pieces. Excess trimmed farthest-first.
     * More pieces = bigger dungeon.
     */
    public int getMaxPieces() {
        return switch (this) {
            case NORMAL    -> 40;
            case HARD      -> 60;
            case NIGHTMARE -> 90;
            case INFERNAL  -> 130;
            case MYTHIC    -> 170;
            case ETERNAL   -> 220;
        };
    }

    /**
     * Number of custom MegaMod dungeon chests to place after boss kill.
     */
    public int getBossChestCount() {
        return switch (this) {
            case NORMAL    -> 2;
            case HARD      -> 3;
            case NIGHTMARE -> 4;
            case INFERNAL  -> 5;
            case MYTHIC    -> 7;
            case ETERNAL   -> 9;
        };
    }

    /**
     * Number of scattered treasure chests in non-boss rooms.
     * Returns {min, max} for random range.
     */
    public int[] getScatteredChestRange() {
        return switch (this) {
            case NORMAL    -> new int[]{1, 3};
            case HARD      -> new int[]{2, 4};
            case NIGHTMARE -> new int[]{4, 7};
            case INFERNAL  -> new int[]{6, 10};
            case MYTHIC    -> new int[]{9, 15};
            case ETERNAL   -> new int[]{13, 20};
        };
    }

    /**
     * Mob cap per room for this tier.
     */
    public int getMobCap() {
        return switch (this) {
            case NORMAL    -> 5;
            case HARD      -> 8;
            case NIGHTMARE -> 12;
            case INFERNAL  -> 16;
            case MYTHIC    -> 20;
            case ETERNAL   -> 20;
        };
    }

    /**
     * Apply tier-appropriate potion effects to a dungeon mob.
     * Call this from every entity's applyDungeonScaling method.
     */
    public void applyMobEffects(LivingEntity mob) {
        int tierLvl = this.level;
        // Tier 3+ (Nightmare): Resistance I
        if (tierLvl >= 3) {
            mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, false));
        }
        // Tier 4+ (Infernal): Speed I + Fire Resistance
        if (tierLvl >= 4) {
            mob.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, false));
            mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, false, false));
        }
        // Tier 5+ (Mythic): Speed II + Strength I
        if (tierLvl >= 5) {
            mob.removeEffect(MobEffects.SPEED);
            mob.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, false));
            mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 0, false, false));
        }
        // Tier 6 (Eternal): Strength II + Resistance II
        if (tierLvl >= 6) {
            mob.removeEffect(MobEffects.STRENGTH);
            mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, false));
            mob.removeEffect(MobEffects.RESISTANCE);
            mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, false));
        }
    }
}

