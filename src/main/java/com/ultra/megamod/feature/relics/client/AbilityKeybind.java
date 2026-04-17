package com.ultra.megamod.feature.relics.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Stub ability keybind — custom relic R+G binds scrapped (task #52). Re-port later.
 * Keeps static KeyMapping fields so other ported code that reads {@code AbilityKeybind.ABILITY_CAST}
 * to gate HUD-related logic (e.g. SpellHotbar) can still compile.
 */
@EventBusSubscriber(modid = "megamod", value = { Dist.CLIENT })
public class AbilityKeybind {
    public static KeyMapping ABILITY_CAST;
    public static KeyMapping ACCESSORY_KEY;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ABILITY_CAST = new KeyMapping("key.megamod.ability_cast", 82, AccessoryKeybind.MEGAMOD_CATEGORY);
        ACCESSORY_KEY = new KeyMapping("key.megamod.accessory_select", 71, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(ABILITY_CAST);
        event.register(ACCESSORY_KEY);
    }
}
