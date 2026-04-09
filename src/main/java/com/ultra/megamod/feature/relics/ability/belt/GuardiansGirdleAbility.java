package com.ultra.megamod.feature.relics.ability.belt;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class GuardiansGirdleAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Fortitude", "Passive damage resistance", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("level", 0.0, 0.0, RelicStat.ScaleType.ADD, 0.0))),
            new RelicAbility("Brace", "Gain enhanced damage resistance", 4,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0))),
            new RelicAbility("Stalwart Stand", "Become an immovable fortress with massive resistance", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("duration", 80.0, 120.0, RelicStat.ScaleType.ADD, 5.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Guardian's Girdle", "Fortitude", GuardiansGirdleAbility::executeFortitude);
        AbilityCastHandler.registerAbility("Guardian's Girdle", "Brace", GuardiansGirdleAbility::executeBrace);
        AbilityCastHandler.registerAbility("Guardian's Girdle", "Stalwart Stand", GuardiansGirdleAbility::executeStalwartStand);
    }

    private static void executeFortitude(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) return;

        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0, false, false, true));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.2, 0.3, 0.2, 0.01);
    }

    private static void executeBrace(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, 1, false, true, true));

        // Ring particles at 3 heights
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 0.3, player.getZ(), 1.2, 12, 1, 0.02);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 1.2, 12, 1, 0.02);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.7, player.getZ(), 1.2, 12, 1, 0.02);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeStalwartStand(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 3, false, true, true));

        // Dome effect using rings at multiple heights
        for (double y = 0.2; y <= 2.0; y += 0.4) {
            double ringRadius = 1.5 * Math.sin(Math.PI * y / 2.2);
            WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD,
                    player.getX(), player.getY() + y, player.getZ(), ringRadius, 14, 1, 0.02);
        }
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.5, 0.8, 0.5, 0.3);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 0.6f);
    }
}
