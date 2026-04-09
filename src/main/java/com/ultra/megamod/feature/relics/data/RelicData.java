/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package com.ultra.megamod.feature.relics.data;

import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicAttributeBonus;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class RelicData {
    private static final String KEY_LEVEL = "relic_level";
    private static final String KEY_XP = "relic_xp";
    private static final String KEY_QUALITY = "relic_quality";
    private static final String KEY_INITIALIZED = "relic_initialized";
    private static final String KEY_STATS = "relic_stats";
    private static final String KEY_ABILITY_POINTS = "relic_ability_points";
    private static final String KEY_EXCHANGES = "relic_exchanges";
    private static final String KEY_ATTRIBUTE_BONUSES = "relic_attribute_bonuses";
    public static final int MAX_LEVEL = 10;
    public static final int BASE_XP_PER_LEVEL = 100;

    /**
     * The first ability in a relic's list is always unlocked regardless of level.
     * All other abilities require the relic to reach their requiredLevel.
     */
    public static boolean isAbilityUnlocked(int relicLevel, RelicAbility ability, List<RelicAbility> abilities) {
        if (!abilities.isEmpty() && abilities.get(0) == ability) {
            return true;
        }
        return relicLevel >= ability.requiredLevel();
    }

    public static int getLevel(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getIntOr(KEY_LEVEL, 0);
    }

    public static void setLevel(ItemStack stack, int level) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putInt(KEY_LEVEL, Math.max(0, Math.min(level, 10)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getXp(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getIntOr(KEY_XP, 0);
    }

    public static void setXp(ItemStack stack, int xp) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putInt(KEY_XP, Math.max(0, xp));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void addXp(ItemStack stack, int amount) {
        int currentLevel;
        int xpNeeded;
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        int currentXp = tag.getIntOr(KEY_XP, 0) + amount;
        for (currentLevel = tag.getIntOr(KEY_LEVEL, 0); currentLevel < 10 && currentXp >= (xpNeeded = 100 + currentLevel * 50); currentXp -= xpNeeded, ++currentLevel) {
        }
        if (currentLevel >= 10) {
            currentXp = 0;
        }
        tag.putInt(KEY_XP, currentXp);
        tag.putInt(KEY_LEVEL, currentLevel);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getQuality(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getIntOr(KEY_QUALITY, 0);
    }

    public static void setQuality(ItemStack stack, int quality) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putInt(KEY_QUALITY, Math.max(0, Math.min(quality, 10)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isInitialized(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getBooleanOr(KEY_INITIALIZED, false);
    }

    public static void initialize(ItemStack stack, List<RelicAbility> abilities, RandomSource random) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag statsTag = new CompoundTag();
        double totalNormalized = 0.0;
        int statCount = 0;
        for (RelicAbility ability : abilities) {
            CompoundTag abilityTag = new CompoundTag();
            for (RelicStat stat : ability.stats()) {
                double rolled = stat.rollInitialValue(random);
                abilityTag.putDouble(stat.name(), rolled);
                double range = stat.maxValue() - stat.minValue();
                totalNormalized = range > 0.0 ? (totalNormalized += (rolled - stat.minValue()) / range) : (totalNormalized += 1.0);
                ++statCount;
            }
            statsTag.put(ability.name(), abilityTag);
        }
        int quality = statCount > 0 ? (int)Math.round(totalNormalized / (double)statCount * 10.0) : 5;
        quality = Math.max(0, Math.min(quality, 10));
        tag.put(KEY_STATS, statsTag);
        tag.putInt(KEY_QUALITY, quality);
        tag.putInt(KEY_LEVEL, 0);
        tag.putInt(KEY_XP, 0);
        tag.putBoolean(KEY_INITIALIZED, true);
        CompoundTag pointsTag = new CompoundTag();
        for (RelicAbility ability : abilities) {
            pointsTag.putInt(ability.name(), 0);
        }
        tag.put(KEY_ABILITY_POINTS, pointsTag);
        // Roll attribute bonuses
        List<RelicAttributeBonus> bonuses = RelicAttributeBonus.rollBonuses(random, quality);
        CompoundTag bonusesTag = new CompoundTag();
        for (int i = 0; i < bonuses.size(); i++) {
            bonusesTag.put(String.valueOf(i), bonuses.get(i).toTag());
        }
        tag.put(KEY_ATTRIBUTE_BONUSES, bonusesTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static double getStatBaseValue(ItemStack stack, String abilityName, String statName) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag statsTag = tag.getCompoundOrEmpty(KEY_STATS);
        CompoundTag abilityTag = statsTag.getCompoundOrEmpty(abilityName);
        return abilityTag.getDoubleOr(statName, 0.0);
    }

    public static int getAbilityPoints(ItemStack stack, String abilityName) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag pointsTag = tag.getCompoundOrEmpty(KEY_ABILITY_POINTS);
        return pointsTag.getIntOr(abilityName, 0);
    }

    public static void setAbilityPoints(ItemStack stack, String abilityName, int points) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag pointsTag = tag.getCompoundOrEmpty(KEY_ABILITY_POINTS);
        pointsTag.putInt(abilityName, Math.max(0, points));
        tag.put(KEY_ABILITY_POINTS, pointsTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getUnspentPoints(ItemStack stack, List<RelicAbility> abilities) {
        int spent = 0;
        for (RelicAbility ability : abilities) {
            spent += RelicData.getAbilityPoints(stack, ability.name());
        }
        return RelicData.getLevel(stack) - spent;
    }

    public static void rerollStat(ItemStack stack, String abilityName, RelicStat stat, RandomSource random) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag statsTag = tag.getCompoundOrEmpty(KEY_STATS);
        CompoundTag abilityTag = statsTag.getCompoundOrEmpty(abilityName);
        double newValue = stat.rollInitialValue(random);
        abilityTag.putDouble(stat.name(), newValue);
        statsTag.put(abilityName, abilityTag);
        tag.put(KEY_STATS, statsTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void resetPoints(ItemStack stack, List<RelicAbility> abilities) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag pointsTag = new CompoundTag();
        for (RelicAbility ability : abilities) {
            pointsTag.putInt(ability.name(), 0);
        }
        tag.put(KEY_ABILITY_POINTS, pointsTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static double getComputedStatValue(ItemStack stack, String abilityName, RelicStat stat) {
        double base = RelicData.getStatBaseValue(stack, abilityName, stat.name());
        int points = RelicData.getAbilityPoints(stack, abilityName);
        return stat.calculateValue(base, points);
    }

    public static int getExchanges(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getIntOr(KEY_EXCHANGES, 0);
    }

    public static void addExchanges(ItemStack stack, int count) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        int current = tag.getIntOr(KEY_EXCHANGES, 0);
        tag.putInt(KEY_EXCHANGES, current + count);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getExchangeCost(ItemStack stack) {
        int exchanges = RelicData.getExchanges(stack);
        return Math.max(1, 5 + (int)(5 * exchanges * 0.01));
    }

    public static int getXpGainPerExchange(ItemStack stack) {
        int level = RelicData.getLevel(stack);
        return Math.max(2, (int) Math.ceil(getXpForNextLevel(level) / 20.0));
    }

    public static int getXpForNextLevel(int level) {
        return 100 + level * 50;
    }

    public static List<RelicAttributeBonus> getAttributeBonuses(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(KEY_ATTRIBUTE_BONUSES);
        List<RelicAttributeBonus> bonuses = new ArrayList<>();
        for (String key : bonusesTag.keySet()) {
            CompoundTag entryTag = bonusesTag.getCompoundOrEmpty(key);
            if (!entryTag.isEmpty()) {
                bonuses.add(RelicAttributeBonus.fromTag(entryTag));
            }
        }
        return bonuses;
    }

    private static final String KEY_COOLDOWN_OVERRIDES = "ability_cooldown_overrides";

    public static int getCooldownOverride(ItemStack stack, String abilityName) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag overrides = tag.getCompoundOrEmpty(KEY_COOLDOWN_OVERRIDES);
        return overrides.getIntOr(abilityName, -1);
    }

    public static void setCooldownOverride(ItemStack stack, String abilityName, int ticks) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag overrides = tag.getCompoundOrEmpty(KEY_COOLDOWN_OVERRIDES);
        if (ticks < 0) {
            overrides.remove(abilityName);
        } else {
            overrides.putInt(abilityName, ticks);
        }
        tag.put(KEY_COOLDOWN_OVERRIDES, overrides);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getEffectiveCooldown(ItemStack stack, String abilityName, int defaultCooldown) {
        int override = getCooldownOverride(stack, abilityName);
        return override >= 0 ? override : defaultCooldown;
    }
}

