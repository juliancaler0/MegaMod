package com.ultra.megamod.mixin.pufferfish_skills;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.ultra.megamod.lib.pufferfish_skills.client.rendering.ItemBatchedRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlRenderPass.class)
public final class GlRenderPassMixin {

	@Inject(method = "drawIndexed", at = @At("HEAD"), cancellable = true)
	private void injectAtDrawIndexed(int i, int j, int k, int l, CallbackInfo ci) {
		if (ItemBatchedRenderer.EMITS != null) {
			var emits = ItemBatchedRenderer.EMITS;
			var stack = RenderSystem.getModelViewStack();
			var pass = (GlRenderPass) (Object) this;

			ItemBatchedRenderer.EMITS = null;

			for (var emit : emits) {
				stack.pushMatrix();
				stack.mul(emit);
				pass.drawIndexed(i, j, k, l);
				stack.popMatrix();
			}

			ItemBatchedRenderer.EMITS = emits;

			ci.cancel();
		}
	}

}
