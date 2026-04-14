package com.ultra.megamod.reliquary.compat.accessories;

import net.neoforged.bus.api.IEventBus;

/**
 * Client-side hook point for Accessories renderer registration.
 *
 * <p>The upstream Reliquary compat registered a 3D belt model via
 * {@code AccessoriesRendererRegistry.registerRenderer(Item, Supplier)}
 * against the legacy {@code AccessoryRenderer} interface (PoseStack + float
 * animation params). MegaMod's internal
 * {@code com.ultra.megamod.lib.accessories} fork is on the 1.21.9+ render-state
 * pipeline where {@code AccessoryRenderer#render} takes an
 * {@code AccessoryRenderState} / {@code SubmitNodeCollector} instead. Porting
 * the 3D belt and the empty-renderer suppressors to the new signature is out
 * of scope for this compile-clean restoration pass, so this class is a no-op
 * shim — items will fall back to the default accessory renderer (item model on
 * the slot). See {@link AccessoryMobCharmBeltRenderer} for the scaffold to
 * finish porting later.
 */
public final class AccessoriesCompatClient {
	private AccessoriesCompatClient() {}

	public static void registerRenderers(IEventBus modBus) {
		// compileOnly accessories API available via the internal fork, but
		// the old AccessoryRenderer signature no longer applies. Left as a
		// hook so reintroducing the 3D belt renderer only needs to uncomment
		// a modBus.addListener here.
	}
}
