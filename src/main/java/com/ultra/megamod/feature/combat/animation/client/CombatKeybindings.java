package com.ultra.megamod.feature.combat.animation.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * BetterCombat keybindings: feint (cancel attack) and mining toggle.
 * Ported from BetterCombat (net.bettercombat.client.Keybindings).
 */
public class CombatKeybindings {

    public static final KeyMapping.Category COMBAT_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("megamod", "combat"));

    public static KeyMapping FEINT_KEY;
    public static KeyMapping MINING_TOGGLE_KEY;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(COMBAT_CATEGORY);
        FEINT_KEY = new KeyMapping("key.megamod.feint", InputConstants.KEY_F, COMBAT_CATEGORY);
        MINING_TOGGLE_KEY = new KeyMapping("key.megamod.mining_toggle", InputConstants.UNKNOWN.getValue(), COMBAT_CATEGORY);
        event.register(FEINT_KEY);
        event.register(MINING_TOGGLE_KEY);
    }
}
