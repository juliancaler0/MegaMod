package com.ultra.megamod.lib.combatroll.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.combatroll.client.gui.HudRenderHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class ClientEventsNeoForge {
    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        if (!Minecraft.getInstance().options.hideGui) {
            HudRenderHelper.render(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
        }
    }
}
