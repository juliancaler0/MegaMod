/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.zombie.Zombie
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 *  net.neoforged.neoforge.event.tick.EntityTickEvent$Post
 */
package com.ultra.megamod.feature.babyzombie;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid="megamod")
public class ImprovedBabyZombies {
    private static final String HEALTH_HALVED_TAG = "megamod:baby_zombie_halved";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Zombie)) {
            return;
        }
        Zombie zombie = (Zombie)entity;
        if (!zombie.isBaby()) {
            return;
        }
        if (zombie.getPersistentData().getBooleanOr(HEALTH_HALVED_TAG, false)) {
            return;
        }
        AttributeInstance maxHealthAttr = zombie.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double halvedHealth = (double)zombie.getMaxHealth() / 2.0;
            maxHealthAttr.setBaseValue(halvedHealth);
            zombie.setHealth((float)halvedHealth);
            zombie.getPersistentData().putBoolean(HEALTH_HALVED_TAG, true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Zombie)) {
            return;
        }
        Zombie zombie = (Zombie)livingEntity;
        if (!zombie.isBaby()) {
            return;
        }
        if (zombie.level().isClientSide()) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(Items.DIAMOND_SWORD) && !mainHand.is(Items.NETHERITE_SWORD)) {
            return;
        }
        if (player.fallDistance > 0.0 && !player.onGround()) {
            event.setNewDamage(zombie.getHealth());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Zombie)) {
            return;
        }
        Zombie zombie = (Zombie)entity;
        if (!zombie.isBaby()) {
            return;
        }
        if (zombie.level().isClientSide()) {
            return;
        }
        if (zombie.getTarget() == null || !zombie.onGround()) {
            return;
        }
        if (zombie.getRandom().nextFloat() < 0.02f) {
            zombie.setDeltaMovement(zombie.getDeltaMovement().add(0.0, 0.42, 0.0));
        }
    }
}

