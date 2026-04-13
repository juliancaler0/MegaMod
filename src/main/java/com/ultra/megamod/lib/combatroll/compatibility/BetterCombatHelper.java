package com.ultra.megamod.lib.combatroll.compatibility;

import com.ultra.megamod.lib.combatroll.Platform;

public class BetterCombatHelper {
    public static void cancelUpswing() {
        if (Platform.isModLoaded("bettercombat")) {
            // BetterCombat integration - not available in MegaMod port
            // Original: ((MinecraftClient_BetterCombat)client).cancelUpswing();
        }
    }

    public static boolean isDoingUpswing() {
        if (Platform.isModLoaded("bettercombat")) {
            // BetterCombat integration - not available in MegaMod port
            // Original: return ((MinecraftClient_BetterCombat)client).getUpswingTicks() > 0;
        }
        return false;
    }
}
