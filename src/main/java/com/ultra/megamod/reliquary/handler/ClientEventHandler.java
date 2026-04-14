package com.ultra.megamod.reliquary.handler;

import net.neoforged.fml.ModContainer;

/**
 * Stub — the client-rendering layer was pruned during the MegaMod port
 * because the 1.21.11 client APIs (RenderType / BakedModel /
 * ItemPropertyFunction / particle TextureSheetParticle / PlayerModel
 * layer registration) all changed shape and would need a ground-up
 * rewrite. Items still register + function; custom renderers, HUD
 * panes and particle providers are follow-up work.
 */
public final class ClientEventHandler {
	private ClientEventHandler() {}

	public static void registerHandlers(ModContainer container) {
		// no-op: see class javadoc
	}
}
