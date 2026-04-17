package com.ultra.megamod.lib.puffish_attributes.neoforge;

import com.ultra.megamod.lib.puffish_attributes.AttributesMod;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge entry point for the ported Puffish Attributes library.
 *
 * <p>Faithfully mirrors the reference mod's {@code NeoForgeMain}: calls {@code AttributesMod.setup()}
 * (class-loads {@link PuffishAttributes}, causing every static attribute field to run
 * {@code DeferredSetup.registerAttribute(...)}, which queues a per-attribute
 * {@link DeferredRegister} in {@link #DEFERRED_REGISTERS}), then registers each queued
 * register with the supplied mod event bus.</p>
 */
public class AttributesForge {

	public static final List<DeferredRegister<?>> DEFERRED_REGISTERS = new ArrayList<>();

	public static void init(IEventBus modEventBus) {
		// Force-load PuffishAttributes so all static fields evaluate and queue their DeferredRegisters
		// into DEFERRED_REGISTERS via NeoForgePlatform#registerReference.
		AttributesMod.setup();
		// Touching a constant forces class initialization without relying on side effects of setup().
		@SuppressWarnings("unused")
		var forceInit = PuffishAttributes.STAMINA;

		for (var deferredRegister : DEFERRED_REGISTERS) {
			deferredRegister.register(modEventBus);
		}
	}
}
