/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.item.ItemStack
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public class InfinityHamAbility {
    private static final List<Holder<MobEffect>> BENEFICIAL_EFFECTS = List.of(MobEffects.SPEED, MobEffects.HASTE, MobEffects.STRENGTH, MobEffects.JUMP_BOOST, MobEffects.RESISTANCE, MobEffects.REGENERATION);
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Feast", "Restore hunger without consuming the item", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("food_value", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5), new RelicStat("saturation", 4.0, 10.0, RelicStat.ScaleType.ADD, 0.8))), new RelicAbility("Alchemy", "Grant a random beneficial potion effect", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("duration", 20.0, 40.0, RelicStat.ScaleType.ADD, 3.0), new RelicStat("amplifier", 0.0, 2.0, RelicStat.ScaleType.ADD, 0.3))));

    public static void register() {
        AbilityCastHandler.registerAbility("Infinity Ham", "Feast", InfinityHamAbility::executeFeast);
        AbilityCastHandler.registerAbility("Infinity Ham", "Alchemy", InfinityHamAbility::executeAlchemy);
    }

    private static void executeFeast(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int foodValue = (int)stats[0];
        float saturation = (float)stats[1];
        player.getFoodData().eat(foodValue, saturation);
        ServerLevel level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeAlchemy(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int durationSeconds = (int)stats[0];
        int amplifier = (int)stats[1];
        ServerLevel level = player.level();
        Holder<MobEffect> effect = BENEFICIAL_EFFECTS.get(level.random.nextInt(BENEFICIAL_EFFECTS.size()));
        player.addEffect(new MobEffectInstance(effect, durationSeconds * 20, amplifier, false, true, true));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}

