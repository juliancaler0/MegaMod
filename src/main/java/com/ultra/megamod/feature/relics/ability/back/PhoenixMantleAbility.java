package com.ultra.megamod.feature.relics.ability.back;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhoenixMantleAbility {
    private static final Map<UUID, Long> REBIRTH_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long REBIRTH_COOLDOWN_TICKS = 6000L;

    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Fireborn", "Permanent fire resistance", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of()),
            new RelicAbility("Rebirth", "Automatically revive when near death", 4,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("heal", 6.0, 14.0, RelicStat.ScaleType.ADD, 1.0))),
            new RelicAbility("Inferno Wings", "Launch upward in a burst of flame damaging nearby mobs", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                            new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Phoenix Mantle", "Fireborn", PhoenixMantleAbility::executeFireborn);
        AbilityCastHandler.registerAbility("Phoenix Mantle", "Rebirth", PhoenixMantleAbility::executeRebirth);
        AbilityCastHandler.registerAbility("Phoenix Mantle", "Inferno Wings", PhoenixMantleAbility::executeInfernoWings);
    }

    private static void executeFireborn(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) return;

        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, false, true));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.2, 0.3, 0.2, 0.01);
    }

    private static void executeRebirth(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        if (player.getHealth() > 1.0f) return;

        UUID playerId = player.getUUID();
        long currentTick = player.level().getServer().overworld().getGameTime();
        Long lastTrigger = REBIRTH_COOLDOWNS.get(playerId);
        if (lastTrigger != null && currentTick - lastTrigger < REBIRTH_COOLDOWN_TICKS) return;

        double healAmount = stats[0];
        REBIRTH_COOLDOWNS.put(playerId, currentTick);

        player.setHealth(Math.min(player.getMaxHealth(), (float) healAmount));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, true, true));

        ServerLevel level = (ServerLevel) player.level();
        WeaponEffects.column(level, (ParticleOptions) ParticleTypes.FLAME,
                player.getX(), player.getY(), player.getZ(), 3.0, 20, 2, 0.15);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 1.5, 16, 2, 0.05);

        level.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 1.0, 0.5, 0.3);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeInfernoWings(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Launch player upward
        player.setDeltaMovement(player.getDeltaMovement().add(0, 1.5, 0));
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, true, true));

        // Damage nearby mobs
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), (float) damage);
            entity.setRemainingFireTicks(60);
        }

        // Particle effects
        WeaponEffects.column(level, (ParticleOptions) ParticleTypes.FLAME,
                player.getX(), player.getY(), player.getZ(), 4.0, 25, 3, 0.2);
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
                player.getX(), player.getY() + 2.0, player.getZ(), 15, 1.0, 0.5, 1.0, 0.1);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
