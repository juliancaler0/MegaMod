package com.ultra.megamod.lib.azurelib.common.platform;

import com.ultra.megamod.lib.azurelib.common.platform.services.*;
import com.ultra.megamod.lib.azurelib.platform.NeoForgeAzureLibInitializer;
import com.ultra.megamod.lib.azurelib.platform.NeoForgeAzureLibNetwork;
import com.ultra.megamod.lib.azurelib.platform.NeoForgePlatformHelper;

/**
 * Platform service accessor. Uses direct NeoForge implementations instead of ServiceLoader.
 */
public final class Services {

    public static final AzureLibInitializer INITIALIZER = new NeoForgeAzureLibInitializer();

    public static final AzureLibNetwork NETWORK = new NeoForgeAzureLibNetwork();

    public static final IPlatformHelper PLATFORM = new NeoForgePlatformHelper();

    private Services() {
        throw new UnsupportedOperationException();
    }
}
