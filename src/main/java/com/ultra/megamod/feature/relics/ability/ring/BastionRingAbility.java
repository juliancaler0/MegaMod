package com.ultra.megamod.feature.relics.ability.ring;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BastionRingAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Piglin Peace", "Piglins won't attack you and nearby piglins grant Luck", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("gold_find", 5.0, 15.0, RelicStat.ScaleType.ADD, 1.5))),
            new RelicAbility("Barter Bonus", "Better piglin barter results with enhanced Luck", 3,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("bonus_chance", 25.0, 60.0, RelicStat.ScaleType.ADD, 5.0))),
            new RelicAbility("Gilded Strike", "Next attack deals heavy gold damage and weakens target", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("bonus_damage", 8.0, 18.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.18))));

    public static void register() {
        AbilityCastHandler.registerAbility("Bastion Ring", "Piglin Peace", BastionRingAbility::executePiglinPeace);
        AbilityCastHandler.registerAbility("Bastion Ring", "Barter Bonus", BastionRingAbility::executeBarterBonus);
        AbilityCastHandler.registerAbility("Bastion Ring", "Gilded Strike", BastionRingAbility::executeGildedStrike);
    }

    private static void executePiglinPeace(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(16.0);
        List<Piglin> piglins = level.getEntitiesOfClass(Piglin.class, area, Piglin::isAlive);
        for (Piglin piglin : piglins) {
            if (piglin.getTarget() == player) {
                piglin.setTarget(null);
            }
        }
        // Grant Luck I when piglins are nearby
        if (!piglins.isEmpty()) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 0, false, false, true));
        }
        level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.3, 0.5, 0.3, 0.0);
    }

    private static void executeBarterBonus(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 200 != 0) {
            return;
        }
        // Apply Luck II (amplifier 1) instead of Luck I
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 210, 1, false, false, true));
    }

    private static void executeGildedStrike(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double bonusDamage = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        double reach = 4.0;
        AABB searchArea = new AABB(eyePos, eyePos.add(lookVec.scale(reach))).inflate(1.0);
        LivingEntity target = null;
        double closestDist = reach + 1.0;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchArea, entity -> entity != player && entity.isAlive() && entity.isPickable())) {
            Vec3 toEntity = candidate.position().add(0.0, (double) candidate.getBbHeight() / 2.0, 0.0).subtract(eyePos);
            double dist = toEntity.length();
            Vec3 normalizedToEntity = toEntity.normalize();
            double dot = lookVec.dot(normalizedToEntity);
            if (dist > reach || !(dot > 0.9) || !(dist < closestDist)) continue;
            closestDist = dist;
            target = candidate;
        }
        if (target == null) {
            return;
        }
        // Deal bonus gold damage
        target.hurt(level.damageSources().playerAttack((Player) player), (float) bonusDamage);
        // Apply Weakness I for 3 seconds (60 ticks)
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true, true));
        // Gold particle burst
        double targetX = target.getX();
        double targetY = target.getY() + (double) target.getBbHeight() / 2.0;
        double targetZ = target.getZ();
        for (int i = 0; i < 30; ++i) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            level.sendParticles((ParticleOptions) ParticleTypes.WAX_ON, targetX + offsetX, targetY + offsetY, targetZ + offsetZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
        // Extra gold sparkle particles
        for (int i = 0; i < 10; ++i) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, targetX + offsetX, targetY + offsetY, targetZ + offsetZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
