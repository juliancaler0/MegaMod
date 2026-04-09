/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BannerItem
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.banners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class EquipableBanners {
    private static final int REQUIRED_SNEAKS = 3;
    private static final int WINDOW_TICKS = 30;
    private static final Map<UUID, SneakTracker> SNEAK_TRACKERS = new HashMap<UUID, SneakTracker>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        UUID playerId = player2.getUUID();
        SneakTracker tracker = SNEAK_TRACKERS.computeIfAbsent(playerId, k -> new SneakTracker());
        boolean currentlySneaking = player2.isShiftKeyDown();
        if (currentlySneaking && !tracker.wasSneaking) {
            long currentTick = player2.level().getGameTime();
            if (tracker.count > 0 && currentTick - tracker.firstSneakTick > 30L) {
                tracker.count = 0;
            }
            if (tracker.count == 0) {
                tracker.firstSneakTick = currentTick;
            }
            ++tracker.count;
            if (tracker.count >= 3) {
                tracker.count = 0;
                EquipableBanners.tryEquipBanner(player2);
            }
        }
        tracker.wasSneaking = currentlySneaking;
    }

    private static void tryEquipBanner(ServerPlayer player) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (mainHand.getItem() instanceof BannerItem) {
            player.setItemSlot(EquipmentSlot.HEAD, mainHand.copy());
            mainHand.setCount(0);
        } else if (offHand.getItem() instanceof BannerItem) {
            player.setItemSlot(EquipmentSlot.HEAD, offHand.copy());
            offHand.setCount(0);
        }
    }

    private static class SneakTracker {
        boolean wasSneaking = false;
        int count = 0;
        long firstSneakTick = 0L;

        private SneakTracker() {
        }
    }
}

