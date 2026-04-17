package com.ultra.megamod.feature.relics.client;

import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Stub HUD overlay — custom relic ability bar scrapped (task #52). Re-port later when
 * source Relics-1.21.1 schema is implemented. Kept so MegaModClient's registration call
 * still compiles; register() is a no-op that doesn't add any GUI layer.
 */
public class AbilityHudOverlay {
    private AbilityHudOverlay() {}

    public static void register(RegisterGuiLayersEvent event) {
        // no-op: overlay scrapped
    }
}
