package com.ultra.megamod.reliquary.client.render;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.reliquary.entity.TippedArrow;

/**
 * Swaps the arrow texture based on whether the arrow carries a potion. In
 * 1.21.11 {@link ArrowRenderer} is generic over both entity + render-state
 * types — colored vs plain arrows fall out of the render-state's
 * {@code xRot}/{@code yRot}/{@code shake} fields plus a texture override.
 *
 * <p>Because the render-state's color flag isn't exposed directly, we stash
 * the entity-side color on a subclass of the vanilla state and read it back
 * in {@link #getTextureLocation}.
 */
public class TippedArrowRenderer extends ArrowRenderer<TippedArrow, TippedArrowRenderer.TippedArrowRenderState> {
	private static final Identifier RES_ARROW = Identifier.parse("textures/entity/projectiles/arrow.png");
	private static final Identifier RES_TIPPED_ARROW = Identifier.parse("textures/entity/projectiles/tipped_arrow.png");

	public TippedArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public TippedArrowRenderState createRenderState() {
		return new TippedArrowRenderState();
	}

	@Override
	public void extractRenderState(TippedArrow entity, TippedArrowRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.color = entity.getColor();
	}

	@Override
	protected Identifier getTextureLocation(TippedArrowRenderState state) {
		return state.color > 0 ? RES_TIPPED_ARROW : RES_ARROW;
	}

	public static class TippedArrowRenderState extends ArrowRenderState {
		public int color;
	}
}
