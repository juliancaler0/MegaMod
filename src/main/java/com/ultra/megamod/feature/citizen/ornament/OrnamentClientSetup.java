package com.ultra.megamod.feature.citizen.ornament;

/**
 * Client-side setup for the Ornament system.
 * Currently delegates to OrnamentRegistry.onRegisterMenuScreens for menu screen registration.
 *
 * Future work:
 * - Dynamic model retexturing (bake material textures onto ornament block models)
 * - Custom ItemRenderer for ornament items with texture preview
 * - Block color handlers for tinted render layers
 */
public class OrnamentClientSetup {

    /**
     * Called from MegaModClient to wire up client-side ornament registrations.
     * The menu screen is registered via OrnamentRegistry.onRegisterMenuScreens
     * which is added as a mod bus listener in MegaModClient.
     */
    public static void init() {
        // Placeholder for future client-side model/render setup.
        // Dynamic retexturing will be added here when the model baking system is implemented.
    }
}
