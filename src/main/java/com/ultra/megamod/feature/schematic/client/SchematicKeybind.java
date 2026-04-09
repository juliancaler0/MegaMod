package com.ultra.megamod.feature.schematic.client;

import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import com.ultra.megamod.feature.schematic.screen.SchematicBrowserScreen;
import com.ultra.megamod.feature.schematic.screen.SchematicPlacementScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Keybind for the schematic system. L key opens browser or placement screen.
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class SchematicKeybind {

    public static KeyMapping OPEN_SCHEMATIC;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_SCHEMATIC = new KeyMapping("key.megamod.schematic", 76, // L key
                AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(OPEN_SCHEMATIC);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_SCHEMATIC != null && OPEN_SCHEMATIC.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                if (SchematicPlacementMode.isActive()) {
                    mc.setScreen(new SchematicPlacementScreen());
                } else {
                    mc.setScreen(new SchematicBrowserScreen());
                }
            } else if (mc.screen instanceof SchematicBrowserScreen
                    || mc.screen instanceof SchematicPlacementScreen) {
                mc.setScreen(null);
            }
        }
    }
}
