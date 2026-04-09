/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.item.alchemy.Potions
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.ProjectileImpactEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 */
package com.ultra.megamod.feature.echoshard;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid="megamod")
public class EchoShardSilence {
    private static final String SILENCED_TAG = "megamod:silenced";

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (serverLevel.getGameTime() % 10L != 0L) {
            return;
        }
        for (Player player : serverLevel.players()) {
            AABB playerArea = player.getBoundingBox().inflate(32.0);
            List<ItemEntity> itemEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, playerArea, itemEntity -> itemEntity.getItem().is(Items.ECHO_SHARD));
            for (ItemEntity shardEntity : itemEntities) {
                LivingEntity target;
                AABB searchBox = shardEntity.getBoundingBox().inflate(1.0);
                List<LivingEntity> nearbyMobs = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> !(entity instanceof Player) && entity.isAlive());
                if (nearbyMobs.isEmpty() || (target = nearbyMobs.getFirst()).getPersistentData().getBooleanOr(SILENCED_TAG, false)) continue;
                ItemStack shardStack = shardEntity.getItem();
                shardStack.shrink(1);
                if (shardStack.isEmpty()) {
                    shardEntity.discard();
                }
                target.setSilent(true);
                target.getPersistentData().putBoolean(SILENCED_TAG, true);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
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
        if (contents == null || !contents.is(Potions.WATER)) {
            return;
        }
        Entity entity2 = hitEntity = event.getRayTraceResult().getType() == HitResult.Type.ENTITY ? ((EntityHitResult)event.getRayTraceResult()).getEntity() : null;
        if (hitEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)hitEntity;
            EchoShardSilence.removeSilence(livingEntity);
            return;
        }
        Level level = potion.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            AABB splashBox = potion.getBoundingBox().inflate(4.0);
            List<LivingEntity> nearbyMobs = serverLevel.getEntitiesOfClass(LivingEntity.class, splashBox, entity -> !(entity instanceof Player) && entity.getPersistentData().getBooleanOr(SILENCED_TAG, false));
            for (LivingEntity mob : nearbyMobs) {
                EchoShardSilence.removeSilence(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof Player) {
            return;
        }
        if (entity.getPersistentData().getBooleanOr(SILENCED_TAG, false)) {
            entity.spawnAtLocation((ServerLevel)entity.level(), new ItemStack((ItemLike)Items.ECHO_SHARD));
        }
    }

    private static void removeSilence(LivingEntity entity) {
        if (entity.getPersistentData().getBooleanOr(SILENCED_TAG, false)) {
            entity.setSilent(false);
            entity.getPersistentData().putBoolean(SILENCED_TAG, false);
        }
    }
}

