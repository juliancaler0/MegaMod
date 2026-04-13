package com.ultra.megamod.lib.spellengine.client;

/**
 * Client network handlers for SpellEngine.
 *
 * In NeoForge, all payload registration (both client and server handlers) is done
 * in {@link com.ultra.megamod.lib.spellengine.network.ServerNetwork#registerPayloadHandlers}.
 * Client-side handler implementations are in
 * {@link com.ultra.megamod.lib.spellengine.network.SpellEngineClientHandler}.
 *
 * This class is kept for backward compatibility with any code that references it.
 */
public class ClientNetwork {
    /**
     * No-op. Handlers are registered via ServerNetwork.registerPayloadHandlers.
     */
    public static void initializeHandlers() {
        // All handlers are now registered in ServerNetwork.registerPayloadHandlers
        // via the NeoForge PayloadRegistrar pattern.
    }
}
