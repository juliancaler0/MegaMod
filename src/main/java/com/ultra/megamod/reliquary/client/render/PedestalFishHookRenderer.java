package com.ultra.megamod.reliquary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.ultra.megamod.reliquary.api.client.IPedestalItemRenderer;
import com.ultra.megamod.reliquary.block.tile.PedestalBlockEntity;

/**
 * Pedestal item renderer for rods (vanilla and Lyssa). In the upstream
 * Reliquary ref this drew the fake fishing-hook texture plus the catenary
 * line out to the target block — both implemented against MultiBufferSource.
 *
 * <p>In 1.21.11 {@link PedestalBlockEntity}'s renderer (PedestalRenderer) is
 * driven by the new render-state + {@code SubmitNodeCollector} pipeline and
 * no longer exposes a MultiBufferSource to per-item extensions, so this hook
 * is a stub for now. The hook position data is still tracked on the block
 * entity via {@link HookRenderingData}; whenever PedestalRenderer is migrated
 * to forward a SubmitNodeCollector through IPedestalItemRenderer, the
 * catenary/line drawing can be restored using
 * {@code collector.submitCustomGeometry(..)} mirrors of the original quads.
 */
public class PedestalFishHookRenderer implements IPedestalItemRenderer {

	@Override
	public void doRender(PedestalBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		// Deferred: pending IPedestalItemRenderer migration to SubmitNodeCollector.
		// Not a blocker — the rod itself still renders via PedestalRenderer's main pass.
	}

	/**
	 * Hook position payload stored on the pedestal block entity. Retained so
	 * the server-side fishing logic can continue to ship the same struct over
	 * the wire without waiting on the renderer migration.
	 */
	public static class HookRenderingData {
		public final double hookX;
		public final double hookY;
		public final double hookZ;

		public HookRenderingData(double hookX, double hookY, double hookZ) {
			this.hookX = hookX;
			this.hookY = hookY;
			this.hookZ = hookZ;
		}
	}
}
