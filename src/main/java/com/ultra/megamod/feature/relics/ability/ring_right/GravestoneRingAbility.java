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
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GravestoneRingAbility {
    private static final Map<UUID, Long> DEATH_WARD_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long DEATH_WARD_COOLDOWN_TICKS = 6000L;

    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Death Ward", "Automatically heal when near death", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("heal", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5))),
            new RelicAbility("Life Tap", "Sacrifice health for increased strength", 4,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("duration", 100.0, 200.0, RelicStat.ScaleType.ADD, 15.0))),
            new RelicAbility("Undying Resolve", "Gain powerful damage resistance and regeneration", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Gravestone Ring", "Death Ward", GravestoneRingAbility::executeDeathWard);
        AbilityCastHandler.registerAbility("Gravestone Ring", "Life Tap", GravestoneRingAbility::executeLifeTap);
        AbilityCastHandler.registerAbility("Gravestone Ring", "Undying Resolve", GravestoneRingAbility::executeUndyingResolve);
    }

    private static void executeDeathWard(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        if (player.getHealth() > 2.0f) return;

        UUID playerId = player.getUUID();
        long currentTick = player.level().getServer().overworld().getGameTime();
        Long lastTrigger = DEATH_WARD_COOLDOWNS.get(playerId);
        if (lastTrigger != null && currentTick - lastTrigger < DEATH_WARD_COOLDOWN_TICKS) return;

        double healAmount = stats[0];
        DEATH_WARD_COOLDOWNS.put(playerId, currentTick);

        player.setHealth(Math.min(player.getMaxHealth(), (float) healAmount + 2.0f));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1, false, true, true));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 1.0, 0.5, 0.3);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeLifeTap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        player.hurt(level.damageSources().magic(), 4.0f);
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, 1, false, true, true));

        WeaponEffects.spiral(level, (ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY(), player.getZ(), 1.0, 2.5, 24, 2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    private static void executeUndyingResolve(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, 3, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 2, false, true, true));

        level.sendParticles((ParticleOptions) ParticleTypes.SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.5, 0.8, 0.5, 0.02);
        level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.8, 1.0, 0.8, 0.05);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
