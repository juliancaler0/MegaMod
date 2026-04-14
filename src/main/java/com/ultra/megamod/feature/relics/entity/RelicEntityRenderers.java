package com.ultra.megamod.feature.relics.entity;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Registers client renderers for the 12 ported relic entities.
 * ThrownItemRenderer only accepts ThrowableItemProjectile subclasses, so the
 * pure ThrowableProjectile / Entity subclasses use NoopRenderer (their visuals
 * come from particles emitted in tick()).
 */
public class RelicEntityRenderers
{
	public static void registerAll(EntityRenderersEvent.RegisterRenderers event)
	{
		// Pure ThrowableProjectile / Entity subclasses — no item visual
		event.registerEntityRenderer(RelicEntityRegistry.SHADOW_GLAIVE.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.SHADOW_SAW.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.STALACTITE.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.DISSECTION.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.SPORE.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.SHOCKWAVE.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.LIFE_ESSENCE.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.BLOCK_SIMULATION.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.ARROW_RAIN.get(), NoopRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.RELIC_XP_ORB.get(), NoopRenderer::new);

		// ThrowableItemProjectile subclasses — render as the default item
		event.registerEntityRenderer(RelicEntityRegistry.SOLID_SNOWBALL.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(RelicEntityRegistry.THROWN_RELIC_XP_BOTTLE.get(), ThrownItemRenderer::new);
	}
}
