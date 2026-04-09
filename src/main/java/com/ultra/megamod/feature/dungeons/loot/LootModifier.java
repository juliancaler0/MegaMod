/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 */
package com.ultra.megamod.feature.dungeons.loot;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record LootModifier(String attributeId, AttributeModifier.Operation operation, double minValue, double maxValue) {
    public static final List<LootModifier> ALL_MODIFIERS = List.of(
        new LootModifier("minecraft:attack_damage",       AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("minecraft:attack_speed",        AttributeModifier.Operation.ADD_VALUE, 0.1, 0.5),
        new LootModifier("minecraft:max_health",          AttributeModifier.Operation.ADD_VALUE, 1.0, 6.0),
        new LootModifier("minecraft:armor",               AttributeModifier.Operation.ADD_VALUE, 1.0, 4.0),
        new LootModifier("minecraft:armor_toughness",     AttributeModifier.Operation.ADD_VALUE, 0.5, 3.0),
        new LootModifier("minecraft:knockback_resistance",AttributeModifier.Operation.ADD_VALUE, 0.05, 0.2),
        new LootModifier("minecraft:movement_speed",      AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, 0.02, 0.08),
        new LootModifier("minecraft:luck",                AttributeModifier.Operation.ADD_VALUE, 1.0, 3.0),
        new LootModifier("megamod:fire_damage_bonus",     AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:ice_damage_bonus",      AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:lightning_damage_bonus", AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:poison_damage_bonus",   AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:critical_damage",       AttributeModifier.Operation.ADD_VALUE, 5.0, 25.0),
        new LootModifier("megamod:critical_chance",       AttributeModifier.Operation.ADD_VALUE, 2.0, 10.0),
        new LootModifier("megamod:lifesteal",             AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:health_regen_bonus",    AttributeModifier.Operation.ADD_VALUE, 0.5, 2.0),
        new LootModifier("megamod:dodge_chance",          AttributeModifier.Operation.ADD_VALUE, 1.0, 5.0),
        new LootModifier("megamod:xp_bonus",              AttributeModifier.Operation.ADD_VALUE, 5.0, 20.0),
        new LootModifier("megamod:loot_fortune",          AttributeModifier.Operation.ADD_VALUE, 2.0, 10.0),
        new LootModifier("megamod:cooldown_reduction",    AttributeModifier.Operation.ADD_VALUE, 2.0, 8.0)
    );

    public double roll(RandomSource random, float tierMultiplier) {
        return roll(random, tierMultiplier, 0.0);
    }

    /**
     * Roll a modifier value with luck bonus.
     * Luck shifts the roll toward the high end of the range.
     * Each point of luck adds ~2% bias toward max value.
     */
    public double roll(RandomSource random, float tierMultiplier, double luckBonus) {
        // Use a gentle loot scaling: Normal=1.0, Hard=1.1, Nightmare=1.2, Infernal=1.35
        // This prevents Infernal gear from being 4x stronger than Normal
        double lootScale = 1.0 + (tierMultiplier - 1.0) * 0.18;
        // Luck biases the random roll toward the max end of the range
        // e.g. 10 luck = 20% bias → roll between 0.2 and 1.0 instead of 0.0 and 1.0
        double minRoll = Math.min(0.8, luckBonus * 0.02); // cap at 80% floor
        double rawRoll = minRoll + (1.0 - minRoll) * random.nextDouble();
        return (this.minValue + (this.maxValue - this.minValue) * rawRoll) * lootScale;
    }
}

