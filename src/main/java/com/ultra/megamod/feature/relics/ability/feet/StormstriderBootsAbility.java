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
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;

public class StormstriderBootsAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Lightning Step", "Shock nearby mobs while moving", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("damage", 0.5, 1.5, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Thunder Leap", "Leap upward and slam with lightning AOE", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Stormstrider Boots", "Lightning Step", StormstriderBootsAbility::executeLightningStep);
        AbilityCastHandler.registerAbility("Stormstrider Boots", "Thunder Leap", StormstriderBootsAbility::executeThunderLeap);
    }

    private static void executeLightningStep(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        float damage = (float) stats[0];
        if (player.getDeltaMovement().horizontalDistanceSqr() <= 0.001) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(2.0);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());
        if (nearby.isEmpty()) {
            return;
        }
        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        LivingEntity target = nearby.getFirst();
        target.hurt(level.damageSources().magic(), damage);

        level.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 0.1, player.getZ(), 3, 0.2, 0.05, 0.2, 0.02);
    }

    private static void executeThunderLeap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Launch upward
        player.setDeltaMovement(player.getDeltaMovement().add(0, 1.5, 0));
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        // Deal AOE damage immediately
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), damage);
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true, true));
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                cx, cy + 0.2, cz, radius, 3, 16, 2);
        level.sendParticles((ParticleOptions) ParticleTypes.FLASH,
                cx, cy + 1.0, cz, 1, 0.0, 0.0, 0.0, 0.0);

        level.playSound(null, cx, cy, cz,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
