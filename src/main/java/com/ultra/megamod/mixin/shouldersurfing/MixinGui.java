package com.ultra.megamod.mixin.shouldersurfing;

import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets the vanilla crosshair render in shoulder-surfing 3rd person by overriding
 * the first-person-only early return in Gui.renderCrosshair.
 */
@Mixin(Gui.class)
public class MixinGui
{
	@Redirect
	(
		method = "renderCrosshair",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"
		),
		require = 0
	)
	private boolean shouldersurfing$doRenderCrosshair(CameraType cameraType)
	{
		return cameraType.isFirstPerson() || ShoulderSurfingImpl.getInstance().getCrosshairRenderer().doRenderCrosshair();
	}
}
