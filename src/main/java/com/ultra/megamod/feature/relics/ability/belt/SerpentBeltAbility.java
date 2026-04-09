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
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;

public class SerpentBeltAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Constrictor", "Slow nearby enemies passively", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("range", 2.0, 4.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Venom Spit", "Poison a nearby enemy", 3,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                            new RelicStat("poison_duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0))),
            new RelicAbility("Coil Strike", "Pull and damage enemies in a cone", 6,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                            new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Serpent Belt", "Constrictor", SerpentBeltAbility::executeConstrictor);
        AbilityCastHandler.registerAbility("Serpent Belt", "Venom Spit", SerpentBeltAbility::executeVenomSpit);
        AbilityCastHandler.registerAbility("Serpent Belt", "Coil Strike", SerpentBeltAbility::executeCoilStrike);
    }

    private static void executeConstrictor(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        LivingEntity nearest = entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

        if (nearest != null) {
            nearest.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true, false));
            level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME,
                    nearest.getX(), nearest.getY() + nearest.getBbHeight() / 2.0, nearest.getZ(),
                    5, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private static void executeVenomSpit(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        int poisonDuration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(8.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        LivingEntity nearest = entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

        if (nearest == null) return;

        nearest.hurt(level.damageSources().magic(), (float) damage);
        nearest.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 1, false, true, false));

        Vec3 from = player.position().add(0, player.getBbHeight() / 2.0, 0);
        Vec3 to = nearest.position().add(0, nearest.getBbHeight() / 2.0, 0);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.ITEM_SLIME, from, to, 12, 2, 0.08);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeCoilStrike(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            // Cone check: entity must be roughly in front of player
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            double dot = look.x * toEntity.x + look.z * toEntity.z;
            if (dot < 0.3) continue; // ~72 degree cone

            entity.hurt(level.damageSources().magic(), (float) damage);

            // Pull toward player
            Vec3 pullDir = player.position().subtract(entity.position()).normalize().scale(0.7);
            entity.setDeltaMovement(entity.getDeltaMovement().add(pullDir));
            entity.hurtMarked = true;
        }

        // Particle effects
        double sweepAngle = Math.PI / 2.5; // ~72 degrees
        WeaponEffects.arc(level, (ParticleOptions) ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 0.5, player.getZ(),
                look.x, look.z, radius, sweepAngle, 16, 2);
        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 0.5, player.getZ(), radius, 10, 2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
