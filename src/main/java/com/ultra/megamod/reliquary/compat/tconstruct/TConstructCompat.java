package com.ultra.megamod.reliquary.compat.tconstruct;

import net.neoforged.bus.api.IEventBus;

/**
 * Stub Tinkers' Construct compat. Mirrors upstream Reliquary — which itself
 * is a stub pending the TConstruct port — and keeps the hook point stable
 * for {@link com.ultra.megamod.reliquary.init.ModCompat} dispatch.
 */
public final class TConstructCompat {
	public TConstructCompat(IEventBus modBus) {
		// no-op
	}

	public void setup() {
		// TODO readd when tconstruct is ported:
		// PedestalRegistry.registerItemWrapper(SwordTool.class, PedestalMeleeWeaponWrapper::new);
		// PedestalRegistry.registerItemWrapper(ScytheTool.class, PedestalMeleeWeaponWrapper::new);
	}
}
