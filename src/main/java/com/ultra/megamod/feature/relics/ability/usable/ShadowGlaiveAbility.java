package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.entity.ShadowGlaiveEntity;
import com.ultra.megamod.feature.relics.entity.ShadowSawEntity;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ShadowGlaiveAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Throw", "Bouncing projectile hitting multiple targets", 1, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 4.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("bounces", 2.0, 5.0, RelicStat.ScaleType.ADD, 0.5))),
        new RelicAbility("Saw Mode", "Spin attack dealing AOE damage", 5, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 6.0, 14.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3))));

    public static void register() {
        AbilityCastHandler.registerAbility("Shadow Glaive", "Throw", ShadowGlaiveAbility::executeThrow);
        AbilityCastHandler.registerAbility("Shadow Glaive", "Saw Mode", ShadowGlaiveAbility::executeSawMode);
    }

    private static void executeThrow(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        int bounces = (int) stats[1];
        ServerLevel level = (ServerLevel) player.level();

        ShadowGlaiveEntity glaive = new ShadowGlaiveEntity(
            com.ultra.megamod.feature.relics.entity.RelicEntityRegistry.SHADOW_GLAIVE.get(), level);
        glaive.setOwner(player);
        Vec3 look = player.getLookAngle();
        glaive.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
        glaive.setDeltaMovement(look.scale(1.2));
        glaive.setParams(damage, bounces, 8.0F);
        level.addFreshEntity(glaive);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeSawMode(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        ShadowSawEntity saw = new ShadowSawEntity(
            com.ultra.megamod.feature.relics.entity.RelicEntityRegistry.SHADOW_SAW.get(), level);
        saw.setOwner(player);
        Vec3 look = player.getLookAngle();
        saw.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
        saw.setDeltaMovement(look.scale(1.4));
        saw.setDamage(damage);
        level.addFreshEntity(saw);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}
