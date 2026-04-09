package com.ultra.megamod.feature.relics.ability.back;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class WindrunnerCloakAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Tailwind", "Passive speed boost", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("speed_level", 0.0, 1.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Gust", "Push all nearby mobs away with a powerful gust", 3,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("knockback", 1.0, 2.0, RelicStat.ScaleType.ADD, 0.15),
                            new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Dash", "Teleport forward in your look direction", 6,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("distance", 6.0, 12.0, RelicStat.ScaleType.ADD, 1.0)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Windrunner Cloak", "Tailwind", WindrunnerCloakAbility::executeTailwind);
        AbilityCastHandler.registerAbility("Windrunner Cloak", "Gust", WindrunnerCloakAbility::executeGust);
        AbilityCastHandler.registerAbility("Windrunner Cloak", "Dash", WindrunnerCloakAbility::executeDash);
    }

    private static void executeTailwind(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        int amplifier = (int) stats[0];
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier, false, false, true));
    }

    private static void executeGust(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double knockback = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            Vec3 pushDir = entity.position().subtract(player.position()).normalize();
            double dx = pushDir.x;
            double dz = pushDir.z;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001) {
                entity.knockback(knockback, -dx / len, -dz / len);
                entity.hurtMarked = true;
            }
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5, player.getZ(), radius, 2, 20, 2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    private static void executeDash(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position();
        Vec3 targetPos = start;

        for (double d = 1.0; d <= range; d += 0.5) {
            Vec3 candidate = start.add(look.scale(d));
            BlockPos feetPos = BlockPos.containing(candidate.x, candidate.y, candidate.z);
            BlockPos headPos = feetPos.above();
            boolean feetClear = level.getBlockState(feetPos).getCollisionShape((BlockGetter) level, feetPos).isEmpty();
            boolean headClear = level.getBlockState(headPos).getCollisionShape((BlockGetter) level, headPos).isEmpty();
            if (!feetClear || !headClear) break;
            targetPos = candidate;
        }

        if (targetPos.distanceTo(start) > 0.5) {
            WeaponEffects.line(level, (ParticleOptions) ParticleTypes.CLOUD,
                    start.add(0, 1, 0), targetPos.add(0, 1, 0), 15, 2, 0.1);
            WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SMOKE,
                    start.add(0, 0.5, 0), targetPos.add(0, 0.5, 0), 10, 1, 0.05);

            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.fallDistance = 0.0f;

            level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
        }
    }
}
