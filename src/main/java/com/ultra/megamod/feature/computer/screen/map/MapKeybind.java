package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * M key opens the full world map directly (without going through the Computer screen).
 */
@EventBusSubscriber(modid = "megamod", value = {Dist.CLIENT})
public class MapKeybind {

    public static KeyMapping OPEN_MAP;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 77 = GLFW_KEY_M
        OPEN_MAP = new KeyMapping("key.megamod.map", 77, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(OPEN_MAP);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_MAP != null && OPEN_MAP.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && mc.player != null) {
                mc.setScreen((Screen) new com.ultra.megamod.feature.computer.screen.MapScreen(null));
            }
        }
    }
}
