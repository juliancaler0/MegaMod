package com.ultra.megamod.lib.azurelib.platform;

import com.ultra.megamod.lib.azurelib.common.platform.services.AzureLibInitializer;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        // Reload listener is registered via NeoForge's AddClientReloadListenersEvent
        // in NeoForgeAzureLibMod. The legacy AzureLibCache.registerReloadListener()
        // approach silently no-ops during mod construction because Minecraft.getInstance()
        // is still null at that lifecycle point.
    }
}
