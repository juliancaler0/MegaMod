package com.ultra.megamod.lib.accessories.owo.ui.event;

import net.minecraft.client.Minecraft;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;

@FunctionalInterface
public interface WindowResizeCallback {
    Event<WindowResizeCallback> EVENT = EventFactory.createArrayBacked(WindowResizeCallback.class, listeners -> (client, window) -> {
        for (var listener : listeners) listener.onResized(client, window);
    });
    void onResized(Minecraft client, com.mojang.blaze3d.platform.Window window);
}
