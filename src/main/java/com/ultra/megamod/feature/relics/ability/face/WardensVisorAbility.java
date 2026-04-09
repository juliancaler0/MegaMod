package com.ultra.megamod.feature.relics.ability.face;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WardensVisorAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Tremor Sense", "Highlight hostile mobs through walls with Glowing",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("detection_range", 8.0, 16.0, RelicStat.ScaleType.ADD, 1.0))),
        new RelicAbility("Sonic Pulse", "AOE damage and slowness to all nearby mobs",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 7.0, RelicStat.ScaleType.ADD, 0.5))),
        new RelicAbility("Sculk Shroud", "Toggle invisibility with a sculk smoke aura",
            7, RelicAbility.CastType.TOGGLE,
            List.of())
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Warden's Visor", "Tremor Sense", WardensVisorAbility::executeTremorSense);
        AbilityCastHandler.registerAbility("Warden's Visor", "Sonic Pulse", WardensVisorAbility::executeSonicPulse);
        AbilityCastHandler.registerAbility("Warden's Visor", "Sculk Shroud", WardensVisorAbility::executeSculkShroud);
    }

    private static void executeTremorSense(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && e instanceof Monster && (double) e.distanceTo((Entity) player) <= range);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 42, 0, false, false, false));
        }
    }

    private static void executeSonicPulse(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), damage);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.SCULK_CHARGE_POP,
                target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
        }
        WeaponEffects.shockwave(level, ParticleTypes.SCULK_CHARGE_POP, player.getX(), player.getY() + 0.3, player.getZ(),
            radius, 4, 20, 2);
        level.sendParticles((ParticleOptions) ParticleTypes.SONIC_BOOM,
            player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeSculkShroud(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Sculk Shroud")) return;
        if (player.tickCount % 20 != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, true));
        WeaponEffects.ring(level, ParticleTypes.SMOKE, player.getX(), player.getY() + 0.3, player.getZ(),
            1.5, 10, 1, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.SCULK_CHARGE_POP,
            player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.3, 0.5, 0.3, 0.01);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.3f, 1.2f);
    }
}
