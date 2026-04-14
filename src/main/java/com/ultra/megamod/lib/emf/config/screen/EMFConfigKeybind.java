package com.ultra.megamod.lib.emf.config.screen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side keybind that opens the EMF config screen.
 * Default: unbound (user must assign).
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class EMFConfigKeybind {

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.megamod.emf_config",
            GLFW.GLFW_KEY_UNKNOWN,
            com.ultra.megamod.feature.relics.client.AccessoryKeybind.MEGAMOD_CATEGORY);

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    // Per-tick counter used by EmfEntityVariantCache for update-frequency gating.
    private static long TICK = 0L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        TICK++;
        com.ultra.megamod.lib.emf.runtime.EmfEntityVariantCache.getInstance().setTick(TICK);
        if (mc.player == null || mc.screen != null) return;
        while (OPEN_CONFIG.consumeClick()) {
            mc.setScreen(new EMFConfigScreen(null));
        }
    }
}
