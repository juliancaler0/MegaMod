package com.ultra.megamod.reliquary.compat.accessories;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.reliquary.Reliquary;

/**
 * Scaffold placeholder for the mob-charm-belt accessory renderer.
 *
 * <p>Holds the {@link ModelLayerLocation} + body-texture {@link Identifier}
 * so {@link AccessoriesCompatClient} (or a future client hook) can register
 * the entity layer definition without searching for the ids. The renderer
 * implementation itself is intentionally not wired up — the internal
 * {@code com.ultra.megamod.lib.accessories} fork is on the new
 * {@code AccessoryRenderState} / {@code SubmitNodeCollector} render pipeline
 * (1.21.9+), which the upstream Reliquary code (PoseStack + HumanoidModel
 * copyFrom) doesn't match. Finishing the port requires reimplementing
 * {@code render} against {@code com.ultra.megamod.lib.accessories.api.client.renderers.AccessoryRenderer}.
 */
public final class AccessoryMobCharmBeltRenderer {
	public static final ModelLayerLocation MOB_CHARM_BELT_LAYER =
			new ModelLayerLocation(Reliquary.getRL("mob_charm_belt"), "main");

	public static final Identifier ON_BODY_TEXTURE =
			Reliquary.getRL("textures/models/armor/mob_charm_belt.png");

	private AccessoryMobCharmBeltRenderer() {}
}
