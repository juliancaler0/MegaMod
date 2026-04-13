package com.ultra.megamod.lib.combatroll.client;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client initialization for CombatRoll.
 * These are MOD bus events - register them via modEventBus.addListener() from MegaModClient.
 * Do NOT use @EventBusSubscriber for these as it only works with the game bus.
 *
 * Example registration in MegaModClient:
 *   modEventBus.addListener(CombatRollClientInit::registerKeys);
 *   modEventBus.addListener(CombatRollClientInit::onClientSetup);
 */
public class CombatRollClientInit {
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        Keybindings.all.forEach(event::register);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        CombatRollClient.initialize();

        // Setup animations on main thread for thread safety
        event.enqueueWork(() -> {
            CombatRollClient.setupAnimations();
        });
    }
}
