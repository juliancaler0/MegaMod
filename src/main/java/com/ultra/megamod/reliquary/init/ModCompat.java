package com.ultra.megamod.reliquary.init;

import com.ultra.megamod.reliquary.compat.accessories.AccessoriesCompat;
import com.ultra.megamod.reliquary.compat.botania.BotaniaCompat;
import com.ultra.megamod.reliquary.compat.curios.CuriosCompat;
import com.ultra.megamod.reliquary.compat.jade.JadeCompat;
import com.ultra.megamod.reliquary.compat.tconstruct.TConstructCompat;
import com.ultra.megamod.reliquary.reference.Compatibility;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/**
 * Runtime dispatch for Reliquary's optional external-mod compat layers.
 *
 * <p>Each module is gated on {@link ModList#isLoaded(String)} so the compat
 * subpackages are only constructed when the companion mod is actually
 * present — their classes have {@code compileOnly} dependencies on the
 * external APIs, and the runtime check keeps the JVM from resolving those
 * types when the mod is absent. JEI's compat is special: its hook is a
 * {@code @JeiPlugin}-annotated class discovered by JEI's own reflection,
 * so there's nothing to dispatch from here — we only log that it's
 * present.
 *
 * <p>Compat status at this revision:
 * <ul>
 *   <li><b>Accessories</b> — active; targets the bundled
 *       {@code com.ultra.megamod.lib.accessories} fork.</li>
 *   <li><b>Curios</b> — active; compile-checked against Curios 14.0.0+1.21.11.</li>
 *   <li><b>JEI</b> — plugin shell only; full category port deferred.</li>
 *   <li><b>Jade</b> — deferred (no 1.21.11 Maven artifact published yet).</li>
 *   <li><b>Botania</b> — stub (upstream is itself a stub).</li>
 *   <li><b>Tinkers' Construct</b> — stub (upstream is itself a stub).</li>
 * </ul>
 */
public final class ModCompat {
	private ModCompat() {}

	public static void initCompats(IEventBus modBus) {
		if (ModList.get().isLoaded(Compatibility.ModIds.ACCESSORIES)) {
			new AccessoriesCompat(modBus);
		}

		if (ModList.get().isLoaded(Compatibility.ModIds.CURIOS)) {
			new CuriosCompat(modBus);
		}

		if (ModList.get().isLoaded(Compatibility.ModIds.BOTANIA)) {
			new BotaniaCompat(modBus).setup();
		}

		if (ModList.get().isLoaded(Compatibility.ModIds.TINKERS_CONSTRUCT)) {
			new TConstructCompat(modBus).setup();
		}

		// Jade compat is a stub at this revision — no 1.21.11 artifact — so
		// constructing it does nothing, but we dispatch symmetrically so the
		// call site matches what it'll look like once the providers land.
		if (ModList.get().isLoaded(Compatibility.ModIds.WAILA) || ModList.get().isLoaded("jade")) {
			new JadeCompat(modBus);
		}

		// JEI's ReliquaryPlugin is picked up automatically by JEI's @JeiPlugin
		// scan; no dispatch here.
	}
}
