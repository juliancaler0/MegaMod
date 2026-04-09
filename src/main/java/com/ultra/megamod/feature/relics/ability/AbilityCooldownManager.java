/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package com.ultra.megamod.feature.relics.ability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class AbilityCooldownManager {
    private static final String KEY_COOLDOWNS = "ability_cooldowns";

    public static int getCooldown(ItemStack stack, String abilityName) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag cooldowns = tag.getCompoundOrEmpty(KEY_COOLDOWNS);
        return cooldowns.getIntOr(abilityName, 0);
    }

    public static void setCooldown(ItemStack stack, String abilityName, int ticks) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag cooldowns = tag.getCompoundOrEmpty(KEY_COOLDOWNS);
        if (ticks > 0) {
            cooldowns.putInt(abilityName, ticks);
        } else {
            cooldowns.remove(abilityName);
        }
        tag.put(KEY_COOLDOWNS, cooldowns);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void tickCooldowns(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag cooldowns = tag.getCompoundOrEmpty(KEY_COOLDOWNS);
        if (cooldowns.isEmpty()) {
            return;
        }
        CompoundTag updated = new CompoundTag();
        boolean changed = false;
        for (String key : cooldowns.keySet()) {
            int remaining = cooldowns.getIntOr(key, 0);
            if (remaining > 1) {
                updated.putInt(key, remaining - 1);
                changed = true;
            } else if (remaining > 0) {
                // Cooldown expired (was 1, now 0) — don't add to updated, mark changed
                changed = true;
            }
        }
        if (changed) {
            if (updated.isEmpty()) {
                tag.remove(KEY_COOLDOWNS);
            } else {
                tag.put(KEY_COOLDOWNS, updated);
            }
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static boolean isOnCooldown(ItemStack stack, String abilityName) {
        return AbilityCooldownManager.getCooldown(stack, abilityName) > 0;
    }

    public static void clearAllCooldowns(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.put(KEY_COOLDOWNS, new CompoundTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static CompoundTag getAllCooldowns(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return tag.getCompoundOrEmpty(KEY_COOLDOWNS);
    }

    /**
     * Reduce all active cooldowns on this item by the given number of ticks.
     * Used by synergy effects (Arcane Flow, Arcane Swordsman) on kill.
     */
    public static void reduceAllCooldowns(ItemStack stack, int ticksToReduce) {
        if (ticksToReduce <= 0) return;
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag cooldowns = tag.getCompoundOrEmpty(KEY_COOLDOWNS);
        if (cooldowns.isEmpty()) return;

        CompoundTag updated = new CompoundTag();
        for (String key : cooldowns.keySet()) {
            int remaining = cooldowns.getIntOr(key, 0);
            int newVal = remaining - ticksToReduce;
            if (newVal > 0) {
                updated.putInt(key, newVal);
            }
        }
        if (updated.isEmpty()) {
            tag.remove(KEY_COOLDOWNS);
        } else {
            tag.put(KEY_COOLDOWNS, updated);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}

