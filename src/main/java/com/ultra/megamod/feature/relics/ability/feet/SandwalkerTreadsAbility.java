package com.ultra.megamod.feature.relics.ability.feet;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class SandwalkerTreadsAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Desert Born", "Gain speed on sand blocks", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("speed_level", 1.0, 2.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Quicksand", "Root all enemies in radius with quicksand", 4,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3),
                    new RelicStat("duration", 60.0, 100.0, RelicStat.ScaleType.ADD, 5.0))),
            new RelicAbility("Sandstorm", "AOE damage with blinding sand", 7,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Sandwalker Treads", "Desert Born", SandwalkerTreadsAbility::executeDesertBorn);
        AbilityCastHandler.registerAbility("Sandwalker Treads", "Quicksand", SandwalkerTreadsAbility::executeQuicksand);
        AbilityCastHandler.registerAbility("Sandwalker Treads", "Sandstorm", SandwalkerTreadsAbility::executeSandstorm);
    }

    private static void executeDesertBorn(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        int amplifier = (int) stats[0];
        BlockState below = player.level().getBlockState(player.blockPosition().below());
        boolean onSand = below.is(Blocks.SAND) || below.is(Blocks.RED_SAND)
                || below.is(Blocks.SANDSTONE) || below.is(Blocks.RED_SANDSTONE);
        if (onSand) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier, false, true, true));
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles((ParticleOptions) ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, 0.01);
        }
    }

    private static void executeQuicksand(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        int duration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 4, false, true, true));
        }

        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                cx, cy + 0.1, cz, radius, 20, 2, 0.05);
        level.playSound(null, cx, cy, cz,
                SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.5f, 0.6f);
    }

    private static void executeSandstorm(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), damage);
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true, true));
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                cx, cy + 0.5, cz, radius, 3, 16, 2);
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.CLOUD,
                cx, cy + 1.0, cz, radius, 2, 12, 1);
        level.playSound(null, cx, cy, cz,
                SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 2.0f, 0.4f);
    }
}
