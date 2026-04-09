/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.necklace;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;

public class ReflectionNecklaceAbility {
    private static final String KEY_ABSORBED_DAMAGE = "absorbed_damage";
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Absorb", "Store a portion of damage taken", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("absorb_rate", 10.0, 25.0, RelicStat.ScaleType.ADD, 2.0))), new RelicAbility("Explode", "Release stored damage as AOE blast", 3, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("radius", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.5), new RelicStat("efficiency", 60.0, 100.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.05))));

    public static void register() {
        AbilityCastHandler.registerAbility("Reflection Necklace", "Absorb", ReflectionNecklaceAbility::executeAbsorb);
        AbilityCastHandler.registerAbility("Reflection Necklace", "Explode", ReflectionNecklaceAbility::executeExplode);
    }

    public static float getAbsorbedDamage(ItemStack stack) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        return (float)tag.getDoubleOr(KEY_ABSORBED_DAMAGE, 0.0);
    }

    public static void addAbsorbedDamage(ItemStack stack, float damage, double absorbRate) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        double current = tag.getDoubleOr(KEY_ABSORBED_DAMAGE, 0.0);
        double absorbed = (double)damage * (absorbRate / 100.0);
        double newValue = Math.min(current + absorbed, 100.0);
        tag.putDouble(KEY_ABSORBED_DAMAGE, newValue);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setAbsorbedDamage(ItemStack stack, float value) {
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putDouble(KEY_ABSORBED_DAMAGE, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void executeAbsorbOnHit(ServerPlayer wearer, ItemStack stack, float damage, double[] stats) {
        double absorbRate = stats.length > 0 ? stats[0] / 100.0 : 0.15;
        double maxStored = stats.length > 1 ? stats[1] : 100.0;

        float toAbsorb = (float)(damage * absorbRate);
        float currentStored = getAbsorbedDamage(stack);
        float newStored = Math.min((float) maxStored, currentStored + toAbsorb);
        setAbsorbedDamage(stack, newStored);

        // Visual: enchanted hit particles
        ServerLevel level = (ServerLevel) wearer.level();
        level.sendParticles(ParticleTypes.ENCHANTED_HIT,
            wearer.getX(), wearer.getY() + 1.0, wearer.getZ(),
            5, 0.3, 0.3, 0.3, 0.1);
    }

    private static void executeAbsorb(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
    }

    private static void executeExplode(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        double efficiency = stats[1];
        float stored = ReflectionNecklaceAbility.getAbsorbedDamage(stack);
        if (stored <= 0.0f) {
            return;
        }
        float damageToInflict = (float)((double)stored * (efficiency / 100.0));
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity entity : entities) {
            entity.hurt(player.damageSources().magic(), damageToInflict);
        }
        CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        tag.putDouble(KEY_ABSORBED_DAMAGE, 0.0);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}

