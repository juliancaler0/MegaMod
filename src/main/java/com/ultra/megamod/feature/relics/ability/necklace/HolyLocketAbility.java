/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.necklace;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;

public class HolyLocketAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Steal", "Nearby mob healing damages them instead", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("steal_rate", 20.0, 50.0, RelicStat.ScaleType.ADD, 4.0))), new RelicAbility("Purify", "Cleanse all negative potion effects", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("cooldown_reduction", 0.0, 30.0, RelicStat.ScaleType.ADD, 4.0))), new RelicAbility("Sanctify", "Heal allies and harm undead in radius", 7, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("heal_amount", 4.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1), new RelicStat("radius", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5))));
    private static final String KEY_LAST_HEALTH = "holy_locket_last_health";

    public static void register() {
        AbilityCastHandler.registerAbility("Holy Locket", "Steal", HolyLocketAbility::executeSteal);
        AbilityCastHandler.registerAbility("Holy Locket", "Purify", HolyLocketAbility::executePurify);
        AbilityCastHandler.registerAbility("Holy Locket", "Sanctify", HolyLocketAbility::executeSanctify);
    }

    private static void executeSteal(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double stealRate = stats[0];
        float currentHealth = player.getHealth();
        float previousHealth = HolyLocketAbility.getLastHealth(stack);
        HolyLocketAbility.setLastHealth(stack, currentHealth);
        if (previousHealth <= 0.0f) {
            return;
        }
        float healAmount = currentHealth - previousHealth;
        if (healAmount <= 0.0f) {
            return;
        }
        float damageToInflict = (float)((double)healAmount * (stealRate / 100.0));
        if (damageToInflict < 0.5f) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(5.0);
        List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area, e -> e.isAlive());
        for (Monster hostile : hostiles) {
            hostile.hurt(player.damageSources().magic(), damageToInflict);
        }
    }

    private static void executePurify(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        ArrayList<MobEffectInstance> negativeEffects = new ArrayList<MobEffectInstance>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (((MobEffect)effect.getEffect().value()).getCategory() != MobEffectCategory.HARMFUL) continue;
            negativeEffects.add(effect);
        }
        for (MobEffectInstance effect : negativeEffects) {
            player.removeEffect(effect.getEffect());
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true, true));
    }

    private static void executeSanctify(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double healAmount = (float)stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        player.heal((float)healAmount);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity entity : entities) {
            if (entity.isInvertedHealAndHarm()) {
                entity.hurt(player.damageSources().magic(), (float)healAmount);
                continue;
            }
            if (entity instanceof Monster) continue;
            entity.heal((float)(healAmount * 0.5));
        }
    }

    private static float getLastHealth(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return (float)tag.getDoubleOr(KEY_LAST_HEALTH, 0.0);
    }

    private static void setLastHealth(ItemStack stack, float health) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putDouble(KEY_LAST_HEALTH, (double)health);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}

