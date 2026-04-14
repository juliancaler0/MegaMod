package com.ultra.megamod.mixin.shouldersurfing;

import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks which EntityRenderState belongs to the camera entity so that the
 * render-time transparency check can find it again during RenderLivingEvent.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState>
{
	@Inject
	(
		method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
		at = @At("RETURN"),
		require = 0
	)
	private void shouldersurfing$trackCameraEntityRenderState(T entity, float partialTick, CallbackInfoReturnable<S> cir)
	{
		if(entity == Minecraft.getInstance().getCameraEntity())
		{
			ShoulderSurfingImpl.getInstance().getCameraEntityRenderer().setCameraEntityRenderState(cir.getReturnValue());
		}
	}
}
