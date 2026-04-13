package com.ultra.megamod.feature.combat.archers.client.compat;

import com.ultra.megamod.feature.combat.archers.item.Quivers;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRendererRegistry;

public class AccessoriesRenderCompat {
    public static void init() {
        for (var entry : Quivers.entries) {
            AccessoriesRendererRegistry.registerRenderer(entry.item(),
                    () -> new AccessoriesQuiverRenderer(entry.id().getPath()));
        }
    }
}
