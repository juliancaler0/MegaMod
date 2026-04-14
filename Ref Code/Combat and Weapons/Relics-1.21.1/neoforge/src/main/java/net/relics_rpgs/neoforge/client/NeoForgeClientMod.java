package net.relics_rpgs.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.relics_rpgs.RelicsMod;
import net.relics_rpgs.client.RelicsClientMod;

@EventBusSubscriber(modid = RelicsMod.NAMESPACE, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RelicsClientMod.init();
    }
}