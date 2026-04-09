package com.ultra.megamod.feature.relics.ability.hands;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class WoolMittenAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Warmth", "Reduces freeze and grants fire protection on fire damage", 1,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("temp_resistance", 40.0, 80.0, RelicStat.ScaleType.ADD, 6.0),
                    new RelicStat("fire_protection", 10.0, 30.0, RelicStat.ScaleType.ADD, 3.0))),
            new RelicAbility("Comfort", "Fast passive health regeneration and poison cleanse", 3,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("regen_rate", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Cozy", "Nearby players gain freeze protection and Regeneration", 5,
                    RelicAbility.CastType.PASSIVE, List.of(
                    new RelicStat("aura_range", 5.0, 12.0, RelicStat.ScaleType.ADD, 1.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Wool Mitten", "Warmth", WoolMittenAbility::executeWarmth);
        AbilityCastHandler.registerAbility("Wool Mitten", "Comfort", WoolMittenAbility::executeComfort);
        AbilityCastHandler.registerAbility("Wool Mitten", "Cozy", WoolMittenAbility::executeCozy);
    }

    private static void executeWarmth(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 10 != 0) {
            return;
        }
        int tempResistance = (int) stats[0];
        // Reduce freeze ticks
        player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - tempResistance));

        // Grant Fire Resistance I for 5 seconds when on fire (cooldown via effect check)
        if (player.isOnFire() && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, true));
        }
    }

    private static void executeComfort(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        // Heal every 60 ticks instead of 100
        if (player.tickCount % 60 != 0) {
            return;
        }
        double regenRate = stats[0];
        if (player.isAlive() && player.getHealth() < player.getMaxHealth()) {
            float healAmount = (float) regenRate;
            player.heal(healAmount);
        }

        // Remove Poison/Wither effects every 200 ticks if regen_rate > 2.0
        if (player.tickCount % 200 == 0 && regenRate > 2.0) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
        }
    }

    private static void executeCozy(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        double auraRange = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(auraRange);
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, area,
                p -> p != player && p.isAlive());
        for (ServerPlayer ally : nearbyPlayers) {
            // Freeze reduction for allies
            ally.setTicksFrozen(Math.max(0, ally.getTicksFrozen() - 10));

            // Apply Regeneration I for 3 seconds (60 ticks) every 100 ticks
            if (player.tickCount % 100 == 0) {
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
            }

            // Particles
            if (player.tickCount % 40 == 0) {
                double midX = (player.getX() + ally.getX()) / 2.0;
                double midY = (player.getY() + ally.getY()) / 2.0 + 1.0;
                double midZ = (player.getZ() + ally.getZ()) / 2.0;
                level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                        midX, midY, midZ, 3, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }
}
