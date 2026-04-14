package com.ultra.megamod.lib.etf.config.screen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side keybind that opens the ETF config screen.
 * Default: unbound (user must assign).
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class ETFConfigKeybind {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.megamod.etf_config",
            GLFW.GLFW_KEY_UNKNOWN,
            com.ultra.megamod.feature.relics.client.AccessoryKeybind.MEGAMOD_CATEGORY);

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (OPEN_CONFIG.consumeClick()) {
            mc.setScreen(new ETFConfigScreen(null));
        }
    }
}
