package com.ultra.megamod.reliquary.compat.curios;

import com.ultra.megamod.reliquary.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Client-side registration for Reliquary Curios renderers.
 *
 * <p>Hooks the entity layer definitions event so the mob charm belt picks up
 * the {@link MobCharmBeltRenderer} shim. The renderer itself is a no-op
 * against the 1.21.11 Curios render-state pipeline — the 3D belt geometry
 * port is deferred until {@code MobCharmBeltModel} is reintroduced. Until
 * then items render with the default curio renderer (flat item model).
 */
public final class CuriosCompatClient {
	private CuriosCompatClient() {}

	public static void registerLayerDefinitions(IEventBus modEventBus) {
		modEventBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event ->
				CuriosRendererRegistry.register(ModItems.MOB_CHARM_BELT.get(), MobCharmBeltRenderer::new));
	}
}
