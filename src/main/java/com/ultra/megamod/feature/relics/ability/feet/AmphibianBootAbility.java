/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.feet;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AmphibianBootAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Swim Speed", "Move faster underwater", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("swim_bonus", 0.02, 0.05, RelicStat.ScaleType.ADD, 0.005))), new RelicAbility("Frog Jump", "Powerful leap in look direction", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("leap_power", 1.5, 3.0, RelicStat.ScaleType.ADD, 0.2))));

    public static void register() {
        AbilityCastHandler.registerAbility("Amphibian Boot", "Swim Speed", AmphibianBootAbility::executeSwimSpeed);
        AbilityCastHandler.registerAbility("Amphibian Boot", "Frog Jump", AmphibianBootAbility::executeFrogJump);
    }

    private static void executeSwimSpeed(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) {
            return;
        }
        if (player.isUnderWater()) {
            double swimBonus = stats[0];
            int amplifier = swimBonus >= 0.04 ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, amplifier, false, false, true));
        }
    }

    private static void executeFrogJump(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double leapPower = stats[0];
        Vec3 lookVec = player.getLookAngle();
        double horizontalPower = leapPower * 0.8;
        double verticalPower = leapPower * 0.4;
        Vec3 impulse = new Vec3(lookVec.x * horizontalPower, verticalPower, lookVec.z * horizontalPower);
        player.setDeltaMovement(player.getDeltaMovement().add(impulse));
        player.hurtMarked = true;
        player.fallDistance = 0.0;
    }
}

