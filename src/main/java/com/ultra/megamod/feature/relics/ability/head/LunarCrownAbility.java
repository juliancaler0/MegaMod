package com.ultra.megamod.feature.relics.ability.head;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LunarCrownAbility {
    private static final Identifier LUNAR_SPEED_ID = Identifier.fromNamespaceAndPath("megamod", "relic_lunar_crown_speed");

    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Lunar Glow", "Grants night vision and speed boost at night",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("speed_bonus", 0.01, 0.03, RelicStat.ScaleType.ADD, 0.005))),
        new RelicAbility("Moonbeam", "Release a pulse of lunar energy, dealing AOE magic damage",
            3, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 6.0, 10.0, RelicStat.ScaleType.ADD, 0.5),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Eclipse", "Cloak yourself in shadow, gaining brief invulnerability",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("duration", 30.0, 50.0, RelicStat.ScaleType.ADD, 2.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Lunar Crown", "Lunar Glow", LunarCrownAbility::executeLunarGlow);
        AbilityCastHandler.registerAbility("Lunar Crown", "Moonbeam", LunarCrownAbility::executeMoonbeam);
        AbilityCastHandler.registerAbility("Lunar Crown", "Eclipse", LunarCrownAbility::executeEclipse);
    }

    private static void executeLunarGlow(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        long dayTime = level.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000L && dayTime <= 23000L;
        if (isNight) {
            double speedBonus = stats[0];
            AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.removeModifier(LUNAR_SPEED_ID);
                attribute.addTransientModifier(new AttributeModifier(LUNAR_SPEED_ID, speedBonus, AttributeModifier.Operation.ADD_VALUE));
            }
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, true));
        } else {
            AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.removeModifier(LUNAR_SPEED_ID);
            }
        }
    }

    private static void executeMoonbeam(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), damage);
            // Moonlight impact on each target
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.2, 0.4, 0.2, 0.05);
        }
        // Expanding lunar pulse rings
        WeaponEffects.shockwave(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
            radius, 3, 20, 2);
        // Central moonlight pillar
        WeaponEffects.column(level, ParticleTypes.END_ROD, player.getX(), player.getY(), player.getZ(),
            4.0, 10, 2, 0.15);
        // Sparkle burst
        level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
            player.getX(), player.getY() + 1.5, player.getZ(), 20, 1.0, 0.5, 1.0, 0.1);
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
            player.getX(), player.getY() + 0.5, player.getZ(), 15, 1.5, 0.8, 1.5, 0.5);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.6f, 1.8f);
    }

    private static void executeEclipse(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        player.setInvulnerable(true);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, false, true));
        com.ultra.megamod.feature.relics.ability.AbilitySystem.scheduleInvulnExpiry(player.getUUID(), level.getGameTime() + duration);
        // Dark vortex spiraling inward
        WeaponEffects.spiral(level, ParticleTypes.WITCH, player.getX(), player.getY(), player.getZ(),
            1.5, 2.5, 24, 2);
        // Shadow cloak burst
        level.sendParticles((ParticleOptions) ParticleTypes.WITCH,
            player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.6, 1.0, 0.6, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE,
            player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.5, 0.8, 0.5, 0.02);
        // Dark ring at feet
        WeaponEffects.ring(level, ParticleTypes.WITCH, player.getX(), player.getY() + 0.1, player.getZ(),
            1.2, 12, 2, 0.03);
        // Reverse portal for eclipse shimmer
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
            player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMBIENT_CAVE.value(), SoundSource.PLAYERS, 0.4f, 1.5f);
    }
}
