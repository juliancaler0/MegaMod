/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.back;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ElytraBoosterAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Acceleration", "Boost forward while elytra flying", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("boost_power", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3))), new RelicAbility("Fuel Efficiency", "Chance to not consume fireworks", 3, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("save_chance", 10.0, 30.0, RelicStat.ScaleType.ADD, 3.0))), new RelicAbility("Glide", "Reduce fall speed while sneaking", 5, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("fall_reduction", 0.3, 0.7, RelicStat.ScaleType.ADD, 0.05))));

    public static void register() {
        AbilityCastHandler.registerAbility("Elytra Booster", "Acceleration", ElytraBoosterAbility::executeAcceleration);
        AbilityCastHandler.registerAbility("Elytra Booster", "Fuel Efficiency", ElytraBoosterAbility::executeFuelEfficiency);
        AbilityCastHandler.registerAbility("Elytra Booster", "Glide", ElytraBoosterAbility::executeGlide);
    }

    private static void executeAcceleration(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!player.isFallFlying()) {
            return;
        }
        double boostPower = stats[0];
        Vec3 look = player.getLookAngle();
        Vec3 boost = look.normalize().scale(boostPower);
        player.setDeltaMovement(player.getDeltaMovement().add(boost));
        player.hurtMarked = true;
    }

    private static void executeFuelEfficiency(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) {
            return;
        }
        if (!player.isFallFlying()) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0, false, false, true));
    }

    private static void executeGlide(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.onGround() || !player.isShiftKeyDown()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.0) {
            double fallReduction = stats[0];
            double reducedY = motion.y * (1.0 - fallReduction);
            player.setDeltaMovement(motion.x, reducedY, motion.z);
            player.hurtMarked = true;
            player.fallDistance *= (double)((float)(1.0 - fallReduction));
        }
    }
}

