package com.ultra.megamod.reliquary.init;

import net.neoforged.bus.api.IEventBus;

/**
 * Stub — all external-mod compat surfaces (Curios, Jade, JEI, Botania,
 * TConstruct, Accessories) were pruned because MegaMod either bundles
 * its own fork (Accessories) or doesn't load the mod at runtime. If
 * any of these are re-introduced as true dependencies, reinstate the
 * appropriate compat file under reliquary.compat.*.
 */
public final class ModCompat {
	private ModCompat() {}

	public static void initCompats(IEventBus modBus) {
		// no-op: see class javadoc
	}
}
