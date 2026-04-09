package com.ultra.megamod.feature.adminmodules;

import com.ultra.megamod.MegaMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class AdminModuleClientEvents {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        // Only run admin module rendering for admin players
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;
        if (!com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(mc.player.getGameProfile().name())) return;

        AdminModuleManager mgr = AdminModuleManager.get();
        for (AdminModule module : mgr.getAllModules()) {
            if (module.isEnabled() && module.isClientSide()) {
                try {
                    module.onRenderWorld(event);
                } catch (Exception ignored) {}
            }
        }
    }
}
