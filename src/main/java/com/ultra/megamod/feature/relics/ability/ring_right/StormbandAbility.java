package com.ultra.megamod.feature.relics.ability.ring_right;

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
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StormbandAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Charged Steps", "Deal lightning damage to nearby mobs while sprinting", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("damage", 1.0, 2.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Arc Discharge", "Chain lightning to multiple targets", 4,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                            new RelicStat("targets", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Galvanic Surge", "Empower yourself with lightning speed and strength", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("duration", 100.0, 200.0, RelicStat.ScaleType.ADD, 15.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Stormband", "Charged Steps", StormbandAbility::executeChargedSteps);
        AbilityCastHandler.registerAbility("Stormband", "Arc Discharge", StormbandAbility::executeArcDischarge);
        AbilityCastHandler.registerAbility("Stormband", "Galvanic Surge", StormbandAbility::executeGalvanicSurge);
    }

    private static void executeChargedSteps(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        if (!player.isSprinting()) return;
        double damage = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(3.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        LivingEntity nearest = entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);
        if (nearest != null) {
            nearest.hurt(level.damageSources().magic(), (float) damage);
            level.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                    nearest.getX(), nearest.getY() + nearest.getBbHeight() / 2.0, nearest.getZ(),
                    8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private static void executeArcDischarge(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        int maxTargets = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(10.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        if (entities.isEmpty()) return;

        entities.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));

        List<LivingEntity> chain = new ArrayList<>();
        LivingEntity current = entities.get(0);
        chain.add(current);

        List<LivingEntity> remaining = new ArrayList<>(entities);
        remaining.remove(current);

        while (chain.size() < maxTargets && !remaining.isEmpty()) {
            LivingEntity lastInChain = chain.get(chain.size() - 1);
            LivingEntity nextTarget = remaining.stream()
                    .min(Comparator.comparingDouble(e -> e.distanceTo(lastInChain)))
                    .orElse(null);
            if (nextTarget == null || nextTarget.distanceTo(lastInChain) > 8.0) break;
            chain.add(nextTarget);
            remaining.remove(nextTarget);
        }

        // Draw line from player to first target
        Vec3 from = player.position().add(0, player.getBbHeight() / 2.0, 0);
        for (int i = 0; i < chain.size(); i++) {
            LivingEntity target = chain.get(i);
            Vec3 to = target.position().add(0, target.getBbHeight() / 2.0, 0);
            WeaponEffects.line(level, (ParticleOptions) ParticleTypes.ELECTRIC_SPARK, from, to, 10, 2, 0.05);
            target.hurt(level.damageSources().magic(), (float) damage);
            from = to;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeGalvanicSurge(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true));

        WeaponEffects.spiral(level, (ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY(), player.getZ(), 1.5, 3.0, 30, 2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8f, 1.5f);
    }
}
