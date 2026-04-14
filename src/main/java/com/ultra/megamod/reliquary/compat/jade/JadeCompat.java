package com.ultra.megamod.reliquary.compat.jade;

import net.neoforged.bus.api.IEventBus;

/**
 * Stub placeholder for the Reliquary Jade compat. See
 * {@code package-info.java} for why this is deferred.
 *
 * <p>The upstream class is annotated with {@code @snownee.jade.api.WailaPlugin}
 * and implements {@code snownee.jade.api.IWailaPlugin}. Both are gated on the
 * Jade API being on the classpath. We don't have a 1.21.11 compile-only
 * Maven coordinate for Jade, so this class holds the public shape (a
 * constructor that accepts the mod bus) and nothing else. Once the Jade API
 * is available, reintroduce the {@code @WailaPlugin} annotation and the four
 * {@code DataProvider*} fields/registrations.
 */
public final class JadeCompat {
	public JadeCompat(IEventBus modBus) {
		// compileOnly jade API unavailable for 1.21.11 — runtime Jade integration
		// is skipped until a dedicated artifact is published. No-op.
	}
}
