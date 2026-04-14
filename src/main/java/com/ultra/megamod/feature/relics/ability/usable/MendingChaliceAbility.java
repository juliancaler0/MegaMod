package com.ultra.megamod.feature.relics.ability.usable;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class MendingChaliceAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Healing Draught", "Drink to restore health", 1,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("heal", 4.0, 10.0, RelicStat.ScaleType.ADD, 0.8))),
            new RelicAbility("Sanctified Ground", "AOE healing for you and nearby allies", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("heal", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.4),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Mending Chalice", "Healing Draught", MendingChaliceAbility::executeHealingDraught);
        AbilityCastHandler.registerAbility("Mending Chalice", "Sanctified Ground", MendingChaliceAbility::executeSanctifiedGround);
    }

    private static void executeHealingDraught(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float heal = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();

        player.heal(heal);

        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();
        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                px, py + 0.5, pz, 5, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                px, py, pz, 10, 0.4, 0.5, 0.4, 0.02);

        level.playSound(null, px, py, pz,
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeSanctifiedGround(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float heal = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        // Heal self
        player.heal(heal);
        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                cx, cy + 2.0, cz, 3, 0.3, 0.2, 0.3, 0.0);

        // Heal nearby allies
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<ServerPlayer> allies = level.getEntitiesOfClass(ServerPlayer.class, area,
                p -> p != player && p.isAlive() && (double) p.distanceTo((Entity) player) <= radius);

        for (ServerPlayer ally : allies) {
            ally.heal(heal);
            level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                    ally.getX(), ally.getY() + 2.0, ally.getZ(), 3, 0.3, 0.2, 0.3, 0.0);

            // Spawn a life-essence orb that drifts to the ally for a trickle-heal
            com.ultra.megamod.feature.relics.entity.LifeEssenceEntity orb =
                new com.ultra.megamod.feature.relics.entity.LifeEssenceEntity(
                    level, cx, cy + 1.0, cz, ally.getId(), heal * 0.25F);
            level.addFreshEntity(orb);
        }

        WeaponEffects.shockwave(level, (ParticleOptions) ParticleTypes.END_ROD,
                cx, cy + 0.2, cz, radius, 3, 16, 1);

        level.playSound(null, cx, cy, cz,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
