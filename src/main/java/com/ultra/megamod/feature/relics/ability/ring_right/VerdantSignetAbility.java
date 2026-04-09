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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class VerdantSignetAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Nature's Blessing", "Regenerate health while standing on grass", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("heal_rate", 0.0, 1.0, RelicStat.ScaleType.ADD, 0.15))),
            new RelicAbility("Growth Surge", "Instantly heal and gain regeneration", 4,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("heal", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5),
                            new RelicStat("regen_duration", 80.0, 160.0, RelicStat.ScaleType.ADD, 10.0))),
            new RelicAbility("Bloom Shield", "Create a healing zone for nearby players", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("heal", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.4),
                            new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Verdant Signet", "Nature's Blessing", VerdantSignetAbility::executeNaturesBlessing);
        AbilityCastHandler.registerAbility("Verdant Signet", "Growth Surge", VerdantSignetAbility::executeGrowthSurge);
        AbilityCastHandler.registerAbility("Verdant Signet", "Bloom Shield", VerdantSignetAbility::executeBloomShield);
    }

    private static void executeNaturesBlessing(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        if (!player.level().getBlockState(player.blockPosition().below()).is(Blocks.GRASS_BLOCK)) return;

        int amplifier = (int) stats[0];
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amplifier, false, true, true));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
    }

    private static void executeGrowthSurge(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double healAmount = stats[0];
        int regenDuration = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();

        player.heal((float) healAmount);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDuration, 1, false, true, true));

        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                player.getX(), player.getY() + 1.5, player.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
                player.getX(), player.getY() + 0.5, player.getZ(), 10, 0.5, 0.5, 0.5, 0.0);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeBloomShield(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double healAmount = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<Player> players = level.getEntitiesOfClass(Player.class, area, e -> e.isAlive());

        for (Player target : players) {
            target.heal((float) healAmount);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true, true));

            level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                    target.getX(), target.getY() + 1.5, target.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 0.2, player.getZ(), radius, 3, 16, 1);
        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.COMPOSTER,
                player.getX(), player.getY() + 0.4, player.getZ(), radius, 2, 12, 1);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.2f, 0.8f);
    }
}
