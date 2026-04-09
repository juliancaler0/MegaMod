/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.fml.common.EventBusSubscriber
 */
package com.ultra.megamod.feature.museum.dimension;

import com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="megamod")
public class MuseumPortalHandler {
    private MuseumPortalHandler() {
    }

    public static boolean shouldEnterMuseum(ServerPlayer player) {
        return !player.isShiftKeyDown();
    }

    public static boolean shouldOpenCatalog(ServerPlayer player) {
        return player.isShiftKeyDown() && player.getMainHandItem().isEmpty();
    }

    public static boolean shouldDonateItem(ServerPlayer player) {
        return player.isShiftKeyDown() && !player.getMainHandItem().isEmpty();
    }

    public static void handleEnterMuseum(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        MuseumDimensionManager manager = MuseumDimensionManager.get(overworld);
        manager.enterMuseum(player);
    }
}

