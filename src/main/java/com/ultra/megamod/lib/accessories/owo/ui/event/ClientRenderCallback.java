package com.ultra.megamod.lib.accessories.owo.ui.event;

import net.minecraft.client.Minecraft;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;

@FunctionalInterface
public interface ClientRenderCallback {
    Event<ClientRenderCallback> START = EventFactory.createArrayBacked(ClientRenderCallback.class, listeners -> client -> { for (var l : listeners) l.render(client); });
    Event<ClientRenderCallback> END = EventFactory.createArrayBacked(ClientRenderCallback.class, listeners -> client -> { for (var l : listeners) l.render(client); });
    void render(Minecraft client);
}
