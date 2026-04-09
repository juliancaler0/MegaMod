/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.belt;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DrownedBeltAbility {
    private static final Identifier ANCHOR_SWIM_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "relic_drowned_belt_anchor");
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Depths", "Grants water breathing", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Pressure", "Underwater AOE damage pulse", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 4.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12), new RelicStat("radius", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.5))), new RelicAbility("Anchor", "Increases swim speed", 6, RelicAbility.CastType.TOGGLE, List.of(new RelicStat("swim_speed", 0.02, 0.05, RelicStat.ScaleType.ADD, 0.005))));

    public static void register() {
        AbilityCastHandler.registerAbility("Drowned Belt", "Depths", DrownedBeltAbility::executeDepths);
        AbilityCastHandler.registerAbility("Drowned Belt", "Pressure", DrownedBeltAbility::executePressure);
        AbilityCastHandler.registerAbility("Drowned Belt", "Anchor", DrownedBeltAbility::executeAnchor);
    }

    private static void executeDepths(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 80 != 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0, false, false, true));
    }

    private static void executePressure(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!player.isUnderWater()) {
            return;
        }
        double damage = stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity entity : entities) {
            entity.hurt(player.damageSources().magic(), (float)damage);
            Vec3 pushDir = entity.position().subtract(player.position()).normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(pushDir.scale(0.8)));
            entity.hurtMarked = true;
        }
    }

    private static void executeAnchor(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double swimSpeed;
        double d = swimSpeed = stats.length > 0 ? stats[0] : 0.03;
        if (player.tickCount % 20 != 0) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(ANCHOR_SWIM_MODIFIER_ID);
            if (player.isInWater()) {
                attribute.addTransientModifier(new AttributeModifier(ANCHOR_SWIM_MODIFIER_ID, swimSpeed, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();
            if (Math.abs(motion.y) < 0.1 && !player.isShiftKeyDown()) {
                player.setDeltaMovement(motion.x, motion.y * 0.3, motion.z);
            }
        }
    }
}

