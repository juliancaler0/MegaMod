package com.ultra.megamod.feature.relics.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Holder for the shared MegaMod keybind category. All MegaMod client keybinds
 * (sort, map, backpack, skill tree, schematic, combat-roll, EMF/ETF config, etc.)
 * group under this category so the vanilla controls screen stays tidy.
 *
 * <p>The accessory menu itself is owned by the lib/accessories library — H opens it
 * via {@code AccessoriesClient.OPEN_SCREEN}. MegaMod no longer registers its own H key.</p>
 */
public final class AccessoryKeybind {
    public static final KeyMapping.Category MEGAMOD_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("megamod", "megamod"));

    private AccessoryKeybind() {}

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(MEGAMOD_CATEGORY);
    }
}
