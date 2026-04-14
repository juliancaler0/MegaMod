/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 */
package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.client.InventoryButtonHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class AccessoryKeybind {
    public static final KeyMapping.Category MEGAMOD_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath((String)"megamod", (String)"megamod"));
    public static KeyMapping OPEN_ACCESSORIES;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(MEGAMOD_CATEGORY);
        // GLFW_KEY_H = 72. Default: press H to open accessories.
        OPEN_ACCESSORIES = new KeyMapping("key.megamod.accessories", org.lwjgl.glfw.GLFW.GLFW_KEY_H, MEGAMOD_CATEGORY);
        event.register(OPEN_ACCESSORIES);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_ACCESSORIES != null && OPEN_ACCESSORIES.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof InventoryScreen) {
                InventoryButtonHandler.togglePanel();
            } else if (mc.screen == null) {
                mc.setScreen((Screen)new InventoryScreen((Player)mc.player));
                InventoryButtonHandler.setPanelOpen(true);
            }
        }
    }
}

