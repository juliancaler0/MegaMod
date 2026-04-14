package com.ultra.megamod.reliquary.compat.botania;

import net.neoforged.bus.api.IEventBus;

/**
 * Stub Botania compat. Mirrors upstream Reliquary — which itself is a stub
 * pending a Botania port — and keeps a consistent entry point for
 * {@link com.ultra.megamod.reliquary.init.ModCompat}.
 */
public final class BotaniaCompat {
	public BotaniaCompat(IEventBus modBus) {
		// no-op
	}

	public void setup() {
		// TODO readd when botania is ported:
		// FortuneCoinItem.addFortuneCoinPickupChecker(
		//         itemEntity -> !BotaniaAPI.instance().hasSolegnoliaAround(itemEntity));
	}
}
