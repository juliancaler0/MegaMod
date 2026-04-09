/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.MerchantScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.npc.villager.Villager
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ScreenEvent$Closing
 *  net.neoforged.neoforge.client.event.ScreenEvent$Init$Post
 *  net.neoforged.neoforge.client.event.ScreenEvent$Render$Post
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.villagerrefresh;

import com.ultra.megamod.feature.villagerrefresh.RefreshTradesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class VillagerTradeRefreshScreen {
    private static Button refreshButton = null;
    private static Button editButton = null;
    private static int cachedVillagerId = -1;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof MerchantScreen)) {
            refreshButton = null;
            editButton = null;
            cachedVillagerId = -1;
            return;
        }
        MerchantScreen merchantScreen = (MerchantScreen)screen;
        Minecraft mcInst = Minecraft.getInstance();
        if (mcInst.player == null || !VillagerTradeRefresh.isAllowed(mcInst.player.getGameProfile().name())) {
            refreshButton = null;
            editButton = null;
            cachedVillagerId = -1;
            return;
        }
        Villager villager = VillagerTradeRefreshScreen.findTradingVillager();
        if (villager == null) {
            refreshButton = null;
            editButton = null;
            cachedVillagerId = -1;
            return;
        }
        cachedVillagerId = villager.getId();

        // Refresh Trades button (left)
        int totalWidth = 230;
        int startX = merchantScreen.width / 2 - totalWidth / 2;
        int y = 4;
        refreshButton = Button.builder((Component)Component.literal("Refresh Trades"), button -> ClientPacketDistributor.sendToServer((CustomPacketPayload)new RefreshTradesPayload(cachedVillagerId), (CustomPacketPayload[])new CustomPacketPayload[0])).bounds(startX, y, 110, 20).build();
        event.addListener((GuiEventListener)refreshButton);

        // Edit Trades button (right) — opens the trade editor screen
        final Villager editVillager = villager;
        editButton = Button.builder((Component)Component.literal("Edit Trades"), button -> {
            Minecraft mc = Minecraft.getInstance();
            // Capture offers from the merchant screen before closing (getOffers() throws on client in 1.21.11)
            MerchantOffers capturedOffers = new MerchantOffers();
            if (mc.screen instanceof MerchantScreen ms) {
                MerchantOffers current = ms.getMenu().getOffers();
                if (current != null) {
                    capturedOffers.addAll(current);
                }
            }
            // Defer screen switch to next tick to avoid issues with container closing mid-frame
            mc.execute(() -> {
                if (mc.player != null) mc.player.closeContainer();
                mc.setScreen(new AdminTradeEditorScreen(editVillager, capturedOffers));
            });
        }).bounds(startX + 120, y, 110, 20).build();
        event.addListener((GuiEventListener)editButton);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof MerchantScreen) {
            if (refreshButton != null) {
                refreshButton.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }
            if (editButton != null) {
                editButton.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof MerchantScreen) {
            refreshButton = null;
            editButton = null;
            cachedVillagerId = -1;
        }
    }

    private static Villager findTradingVillager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        Entity entity = mc.crosshairPickEntity;
        if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            return villager;
        }
        for (Entity entity2 : mc.level.entitiesForRendering()) {
            Villager villager;
            if (!(entity2 instanceof Villager) || !mc.player.equals((Object)(villager = (Villager)entity2).getTradingPlayer())) continue;
            return villager;
        }
        return null;
    }
}

