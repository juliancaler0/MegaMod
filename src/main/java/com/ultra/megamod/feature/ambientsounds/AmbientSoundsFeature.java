package com.ultra.megamod.feature.ambientsounds;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientTickHandler;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class AmbientSoundsFeature {

    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called from MegaModClient to register mod-bus events (GUI layer).
     */
    public static void init(IEventBus modEventBus) {
        // Register GUI layer for debug overlay
        modEventBus.addListener(AmbientSoundsFeature::onRegisterGuiLayers);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath("megamod", "ambient_debug"),
            AmbientTickHandler::renderOverlay
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        AmbientTickHandler.onTick();
    }

    /** Trigger an async reload of the ambient sound engine. */
    public static void reload() {
        AmbientTickHandler.reload();
    }

    /** Schedule a reload on the next tick. */
    public static void scheduleReload() {
        AmbientTickHandler.scheduleReload();
    }

}
