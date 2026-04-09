package com.ultra.megamod.feature.dungeons.insurance.client;

import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceOpenPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class InsuranceClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!InsuranceOpenPayload.shouldOpenScreen) {
            return;
        }
        InsuranceOpenPayload.shouldOpenScreen = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.setScreen(new InsuranceScreen(
                InsuranceOpenPayload.clientTierName,
                InsuranceOpenPayload.clientSlotCosts,
                InsuranceOpenPayload.clientPartyNames
        ));
    }
}
