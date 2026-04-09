/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.item.alchemy.Potions
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.ProjectileImpactEvent
 */
package com.ultra.megamod.feature.invisframes;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid="megamod")
public class InvisibleFrames {
    private static final double SPLASH_RADIUS = 4.0;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Level level;
        Entity hitEntity;
        Projectile projectile = event.getProjectile();
        if (!(projectile instanceof ThrownSplashPotion)) {
            return;
        }
        ThrownSplashPotion potion = (ThrownSplashPotion)projectile;
        if (potion.level().isClientSide()) {
            return;
        }
        ItemStack potionItem = potion.getItem();
        PotionContents contents = (PotionContents)potionItem.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return;
        }
        boolean isInvisibility = InvisibleFrames.isInvisibilityPotion(contents);
        boolean isWaterBottle = contents.is(Potions.WATER);
        if (!isInvisibility && !isWaterBottle) {
            return;
        }
        if (event.getRayTraceResult().getType() == HitResult.Type.ENTITY && InvisibleFrames.isTargetEntity(hitEntity = ((EntityHitResult)event.getRayTraceResult()).getEntity())) {
            if (isInvisibility) {
                hitEntity.setInvisible(true);
            } else if (isWaterBottle) {
                hitEntity.setInvisible(false);
            }
        }
        if ((level = potion.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            AABB splashBox = potion.getBoundingBox().inflate(4.0);
            List<Entity> nearbyEntities = serverLevel.getEntities((Entity)potion, splashBox, InvisibleFrames::isTargetEntity);
            for (Entity entity : nearbyEntities) {
                if (isInvisibility) {
                    entity.setInvisible(true);
                    continue;
                }
                if (!isWaterBottle || !entity.isInvisible()) continue;
                entity.setInvisible(false);
            }
        }
    }

    private static boolean isTargetEntity(Entity entity) {
        return entity instanceof ArmorStand || entity instanceof ItemFrame;
    }

    private static boolean isInvisibilityPotion(PotionContents contents) {
        if (contents.is(Potions.INVISIBILITY) || contents.is(Potions.LONG_INVISIBILITY)) {
            return true;
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect() != MobEffects.INVISIBILITY) continue;
            return true;
        }
        return false;
    }
}

