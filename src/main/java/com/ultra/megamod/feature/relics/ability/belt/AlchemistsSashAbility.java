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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AlchemistsSashAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Potion Mastery", "Extend duration of active potion effects", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("bonus_ticks", 1.0, 2.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Transmute", "Remove all negative effects and gain regeneration", 4,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("regen_duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0))),
            new RelicAbility("Volatile Mix", "Throw a volatile potion mix damaging and debuffing enemies", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                            new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Alchemist's Sash", "Potion Mastery", AlchemistsSashAbility::executePotionMastery);
        AbilityCastHandler.registerAbility("Alchemist's Sash", "Transmute", AlchemistsSashAbility::executeTransmute);
        AbilityCastHandler.registerAbility("Alchemist's Sash", "Volatile Mix", AlchemistsSashAbility::executeVolatileMix);
    }

    private static void executePotionMastery(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        int bonusTicks = (int) stats[0];

        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) return;

        // Extend all active effects
        for (MobEffectInstance effect : effects) {
            int remaining = effect.getDuration();
            if (remaining > 0 && remaining < 32000) { // Don't extend infinite effects
                // Re-apply with extended duration
                player.addEffect(new MobEffectInstance(
                        effect.getEffect(), remaining + bonusTicks, effect.getAmplifier(),
                        effect.isAmbient(), effect.isVisible(), effect.showIcon()));
            }
        }

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.WITCH,
                player.getX(), player.getY() + 1.5, player.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
    }

    private static void executeTransmute(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int regenDuration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        // Collect and remove negative effects
        Collection<MobEffectInstance> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (MobEffectInstance effect : activeEffects) {
            if (!effect.getEffect().value().isBeneficial()) {
                player.removeEffect(effect.getEffect());
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDuration, 0, false, true, true));

        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.8, 0.5, 0.5);
        level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.4, 0.5, 0.4, 0.0);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeVolatileMix(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Target area in front of player
        Vec3Helper lookPos = getLookTarget(player, 5.0);
        double tx = lookPos.x;
        double ty = lookPos.y;
        double tz = lookPos.z;

        AABB area = new AABB(tx - radius, ty - radius, tz - radius, tx + radius, ty + radius, tz + radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), (float) damage);
            // Apply random negative effect
            int roll = level.random.nextInt(3);
            switch (roll) {
                case 0 -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, false));
                case 1 -> entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, false, true, false));
                case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true, false));
            }
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.SPLASH,
                tx, ty + 0.5, tz, radius, 3, 16, 2);
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.ITEM_SLIME,
                tx, ty + 0.3, tz, radius, 2, 12, 2);

        level.playSound(null, tx, ty, tz,
                SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static Vec3Helper getLookTarget(ServerPlayer player, double distance) {
        net.minecraft.world.phys.Vec3 look = player.getLookAngle().normalize();
        return new Vec3Helper(
                player.getX() + look.x * distance,
                player.getY() + player.getEyeHeight() / 2.0 + look.y * distance,
                player.getZ() + look.z * distance
        );
    }

    private record Vec3Helper(double x, double y, double z) {}
}
