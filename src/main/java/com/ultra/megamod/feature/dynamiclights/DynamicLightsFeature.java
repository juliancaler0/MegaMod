package com.ultra.megamod.feature.dynamiclights;

import com.ultra.megamod.MegaMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = MegaMod.MODID)
public class DynamicLightsFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("megamod-dynamiclights");

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("[MegaMod] Dynamic Lights: Sending config to world...");
        DynamicLightsConfigSender.sendConfig(event.getServer());
        LOGGER.info("[MegaMod] Dynamic Lights loaded successfully!");
    }
}
