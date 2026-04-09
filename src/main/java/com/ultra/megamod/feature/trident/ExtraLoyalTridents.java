/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.arrow.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 */
package com.ultra.megamod.feature.trident;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid="megamod")
public class ExtraLoyalTridents {
    private static final double VOID_THRESHOLD_Y = -60.0;
    private static final String VOID_RESCUE_TAG = "megamod:void_rescued";

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
        for (ServerPlayer player : serverLevel.players()) {
            AABB searchArea = player.getBoundingBox().inflate(128.0, 256.0, 128.0);
            List<ThrownTrident> tridents = serverLevel.getEntitiesOfClass(ThrownTrident.class, searchArea, trident -> trident.getY() < -60.0 && trident.isAlive());
            for (ThrownTrident trident2 : tridents) {
                Entity owner;
                if (!ExtraLoyalTridents.hasLoyalty(trident2, serverLevel) || !((owner = trident2.getOwner()) instanceof ServerPlayer)) continue;
                ServerPlayer ownerPlayer = (ServerPlayer)owner;
                if (!ownerPlayer.isAlive()) continue;
                Vec3 ownerPos = ownerPlayer.position();
                trident2.setPos(ownerPos.x, ownerPos.y + 1.5, ownerPos.z);
                trident2.setDeltaMovement(0.0, -0.05, 0.0);
                trident2.getPersistentData().putBoolean(VOID_RESCUE_TAG, true);
                trident2.setNoGravity(false);
            }
        }
    }

    private static boolean hasLoyalty(ThrownTrident trident, ServerLevel level) {
        ItemStack tridentStack = trident.getWeaponItem();
        if (tridentStack.isEmpty()) {
            return false;
        }
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Enchantment> enchantmentRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> loyaltyHolder = enchantmentRegistry.get(Enchantments.LOYALTY);
        if (loyaltyHolder.isEmpty()) {
            return false;
        }
        return tridentStack.getEnchantments().getLevel(loyaltyHolder.get()) > 0;
    }
}

